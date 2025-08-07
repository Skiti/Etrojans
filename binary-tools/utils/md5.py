import json

def create_json_file(file_name, data):
    with open(file_name, 'w') as json_file:
        json.dump(data, json_file, indent=4)

