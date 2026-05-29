import sys
from dotenv import load_dotenv
from openai import OpenAI

load_dotenv()

FORBIDDEN_STRING = "REPLACE-ALL-THE-FINE-TUNED-MODEL"
def validate_jsonl_file(filepath: str):
    with open(filepath, "r", encoding="utf-8") as f:
        for line_number, line in enumerate(f, start=1):
            if FORBIDDEN_STRING in line:
                raise ValueError(
                    f"Error: Forbidden string '{FORBIDDEN_STRING}' found in file "
                    f"'{filepath}' on line {line_number}. Batch upload aborted."
                )

jsonl_path = "batchForOpenAI.jsonl"
validate_jsonl_file(jsonl_path)


client = OpenAI()

with open(jsonl_path, "rb") as f:
    batch_input_file = client.files.create(
        file=f,
        purpose="batch"
    )

response = client.batches.create(
    input_file_id=batch_input_file.id,
    endpoint="/v1/chat/completions",
    completion_window="24h"
)

print(response)