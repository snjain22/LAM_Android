# from langchain.llms import llamacpp
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_community.chat_models import ChatOllama
from langchain_community.llms import Ollama
import streamlit as st
import os
from dotenv import load_dotenv
from WindowsFunctions import set_brightness, set_volume, open_application, press_key

load_dotenv()

os.environ["LANGCHAIN_TRACING_V2"] = "true"
os.environ["LANGCHAIN_API_KEY"] = os.getenv("LANGCHAIN_API_KEY")

def execute_command(command):
    if "brightness" in command:
        value = int(''.join(filter(str.isdigit, command)))
        return set_brightness(value)
    elif "volume" in command:
        value = int(''.join(filter(str.isdigit, command)))
        return set_volume(value)
    elif "open" in command:
        if "chrome" in command.lower():
            return open_application("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe")
        # Add more applications as needed
    elif "press" in command:
        key = command.split()[-1]
        return press_key(key)
    else:
        return "Unknown command"


prompt = ChatPromptTemplate.from_messages(
    [
        ("system", "You are a helpful assistant. Please respond to the user queries."),
        ("user", "Question: {question}")
    ]
)

#Streamlit Framework
st.title("LLAMA3.1 Locally Run")
input_text = st.text_input("Enter whatever you want to search: ")

#LLAMA3.1 8B LLM
llm = Ollama(model = "llama3.1")
output_parser = StrOutputParser()
chain = prompt | llm | output_parser

#direct output
# st.write(chain.invoke(input_text))

#as and when output comes, output
if input_text:
    # Get the LLM response
    response = chain.invoke({"question": input_text})
    st.write("LLM Response: " + response)

    # Execute the response as a command
    result = execute_command(response)
    st.write("Execution Result: " + result)

