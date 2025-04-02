import mediapipe as mp
from mediapipe.tasks.python.genai import converter


# from transformers import AutoModelForCausalLM, AutoTokenizer
# from peft import PeftModel
# import torch

# # 🔹 Define Paths
# base_model_name = "google/gemma-2b"  # The original base model
# lora_model_path = "SavedModel_Feb25"  # Directory where LoRA adapter is saved
# merged_model_path = "SavedModel_Feb25_merged"  # New directory to save merged model

# # 🔹 Load Base Model
# base_model = AutoModelForCausalLM.from_pretrained(
#     base_model_name,
#     low_cpu_mem_usage=True,
#     return_dict=True,
#     torch_dtype=torch.float16,
#     device_map={"": 0}  # Send to GPU
# )

# # 🔹 Load LoRA Adapter and Merge into Base Model
# model = PeftModel.from_pretrained(base_model, lora_model_path)
# merged_model = model.merge_and_unload()  # Merge LoRA into base model

# # 🔹 Save Merged Model
# merged_model.save_pretrained(merged_model_path)
# tokenizer = AutoTokenizer.from_pretrained(base_model_name)
# tokenizer.save_pretrained(merged_model_path)

# print(f"Merged model saved to: {merged_model_path}")


# Define paths and parameters
conversion_config = converter.ConversionConfig(
    input_ckpt="SavedModel_Feb25_merged",  # Use merged model path
    ckpt_format="safetensors",
    model_type="GEMMA_2B",
    backend='gpu',
    output_dir="SavedModel_Feb25/converted",
    combine_file_only=False,
    vocab_model_file="SavedModel_Feb25_merged",
    output_tflite_file="SavedModel_Feb25/samsunggem.bin",
)
# Convert the checkpoint
converter.convert_checkpoint(conversion_config)

print("Model converted successfully and saved")
