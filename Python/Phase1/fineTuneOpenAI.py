from dotenv import load_dotenv
import os
from openai import OpenAI

# Load OPENAI_API_KEY from .env
load_dotenv()

client = OpenAI()

training=client.files.create(
  file=open("toFineTuneOpenAI_training.json", "rb"),
  purpose="fine-tune"
)

validation=client.files.create(
  file=open("toFineTuneOpenAI_validation.json", "rb"),
  purpose="fine-tune"
)

training_file_id = training.id
validation_file_id = validation.id
print("\n\n\nSent to OpenAI! File ids are: ", training_file_id, " (training) and ", validation_file_id, "(validation)")

from openai import OpenAI
client = OpenAI()

client.fine_tuning.jobs.create(
  training_file=training_file_id,
  validation_file=validation_file_id,
  model="gpt-4.1-2025-04-14"
)

print("\nFine-tuned on OpenAI!")
