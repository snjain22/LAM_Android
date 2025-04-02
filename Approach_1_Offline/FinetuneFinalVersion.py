import torch
import pandas as pd
import numpy as np
import warnings
import json
import time
from datasets import Dataset

from transformers import (
    AutoModelForCausalLM,
    AutoTokenizer,
    DataCollatorForLanguageModeling,
    BitsAndBytesConfig,
    TrainingArguments,
    pipeline,
    logging,
)
from peft import LoraConfig, get_peft_model, AutoPeftModelForCausalLM, PeftModel
from trl import SFTTrainer

# 🔹 Check CUDA availability
print("Torch version:", torch.__version__)
print("CUDA Available:", torch.cuda.is_available())
print("Device Name:", torch.cuda.get_device_name() if torch.cuda.is_available() else "CPU")
print("CUDA Memory Info:", torch.cuda.mem_get_info() if torch.cuda.is_available() else "N/A")

# 🔹 Model Paths
base_model = "google/gemma-2b"
output_directory = "SavedModel_Feb25"  # Directory to save model

# 🔹 BitsAndBytes Quantization Configuration
bnb_config = BitsAndBytesConfig(
    load_in_4bit=True,
    bnb_4bit_quant_type="nf4",
    bnb_4bit_compute_dtype=torch.bfloat16,
    bnb_4bit_use_double_quant=True,
)

# 🔹 Load Model
model = AutoModelForCausalLM.from_pretrained(
    base_model,
    quantization_config=bnb_config,
    device_map="auto",
)

# 🔹 Load Tokenizer
tokenizer = AutoTokenizer.from_pretrained(base_model, trust_remote_code=True)

# 🔹 Load Dataset
data = []
with open("fine.jsonl") as file:
    for line in file:
        line = line.strip()
        features = json.loads(line)
        prompt = f"user\n{features['instruction']}\nmodel\n{features['response']}"
        data.append({'prompt': prompt})

# Convert dataset
data_dict = {'prompt': [item['prompt'] for item in data]}
dataset = Dataset.from_dict(data_dict)

# 🔹 Tokenize and Split Dataset
dataset = dataset.map(lambda samples: tokenizer(samples["prompt"]), batched=True)
dataset = dataset.train_test_split(test_size=0.2)
train_data = dataset["train"]
test_data = dataset["test"]

# 🔹 Clear CUDA Cache
torch.cuda.empty_cache()

# 🔹 LoRA Configuration
target_modules = ["q_proj", "k_proj", "v_proj", "o_proj"]
lora_config = LoraConfig(
    r=128,
    lora_dropout=0.1,
    target_modules=target_modules,
    task_type="CAUSAL_LM"
)

# Apply LoRA
model = get_peft_model(model, lora_config)

# 🔹 Training Arguments
training_args = TrainingArguments(
    per_device_train_batch_size=2,
    gradient_accumulation_steps=4,
    warmup_steps=100,
    learning_rate=2e-5,
    fp16=True,
    num_train_epochs=100,
    logging_steps=100,
    output_dir=output_directory,
    save_strategy="epoch",
    save_total_limit=3,  # Keep last 3 checkpoints
)

# 🔹 Train Model
trainer = SFTTrainer(
    model=model,
    train_dataset=train_data,
    eval_dataset=test_data,
    dataset_text_field="prompt",
    peft_config=lora_config,
    args=training_args,
)

trainer.train()



try:
    output_directory = "SavedModel_Feb25"  # Define your saving directory

    print("Saving using Hugging Face's API...")
    model.save_pretrained(output_directory)
    tokenizer.save_pretrained(output_directory)

    print(f"Saved model and tokenizer in {output_directory}, ALSO saving through Torch.")
    torch.save(model.state_dict(), f"{output_directory}/gemma2b_finetuned.pth")

    print("Pushing Model to Hugging Face Hub...")
    model.push_to_hub(output_directory, use_temp_dir=False)

    print("Pushing Tokenizer to Hugging Face Hub...")
    tokenizer.push_to_hub(output_directory, use_temp_dir=False)

except Exception as e:
    print(f"Could not save using HF due to error: {e}. Saving through Torch instead.")
    torch.save(model.state_dict(), f"{output_directory}/gemma2b_finetuned.pth")

# 🔹 Convert to TFLite using MediaPipe
from mediapipe.tasks.python.genai import converter

conversion_config = converter.ConversionConfig(
    input_ckpt=output_directory,
    ckpt_format="safetensors",
    model_type="GEMMA_2B",
    backend='gpu',
    output_dir=f"{output_directory}/converted",
    combine_file_only=False,
    vocab_model_file=output_directory,
    output_tflite_file=f"{output_directory}/samsunggem.bin",
)

converter.convert_checkpoint(conversion_config)

print("Model converted successfully and saved in", f"{output_directory}/converted")
