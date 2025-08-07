from utils.patch import *
from utils.md5 import *
from utils.zip import *
import hashlib

global binary_data

# function for firmware creation
def create_firmware(firmware, scooter_version):
    global binary_data
    if(scooter_version == "M365" or scooter_version == "Pro"):
        res = create_126_firmware(firmware,binary_data)
    else:
        res = create_141_firmware(firmware,binary_data)

    if(res):
        filename = "FIRM.bin"
        write_modified_data(filename)
        print("File saved: " + filename + '\n')
        md5_hex = hashlib.md5(binary_data).hexdigest()
        print("MD5 hash:", md5_hex + '\n')
        if(scooter_version == "M365"):
            firmware_version_numb = "1.2.6"
        elif(scooter_version == "Pro"):
            firmware_version_numb = "1.4.1"
        else:
            firmware_version_numb = "1.2.6"
        data_to_save = {
            "schemaVersion": 1,
            "firmware": {
                "displayName": firmware_version_numb,
                "model": scooter_version,
                "enforceModel": "false",
                "type": "BMS",
                "compatible": [
                    "mi_BMS_ST8"
                ],
                "encryption": "plain",
                "md5": {
                    "bin": md5_hex
                }
            }
        }

        create_json_file("info.json", data_to_save)
        filename = firmware + "-" + scooter_version + ".zip"
        create_zip_with_files(filename)


def read_binary_file(file_path):
    global binary_data
    try:
        with open(file_path, 'rb') as file:
            # Read the content of the binary file
            binary_data = bytearray(file.read())
            print(f"File '{file_path}' read successfully.")

    except FileNotFoundError:
        print(f"Error: The file '{file_path}' does not exist.")
    except Exception as e:
        print(f"Error while reading the file: {e}")

def write_modified_data(file_path):
    global binary_data
    try:
        with open(file_path, 'wb') as file:
            # Write the modified global variable to the file
            file.write(binary_data)
            print(f"File '{file_path}' written with the modified byte sequence.")

    except Exception as e:
        print(f"Error while writing the file: {e}")


def open_firmware(scooter_version):

    if(scooter_version == "M365" or scooter_version == "Pro"):
        read_binary_file("1.2.6.bin")
    else:
        read_binary_file("1.4.1.bin")
