from utils.managebytes import *

#
# Main function for menu
#
def main():

    # Menu header
    print("===== Select scooter version =====")

    scooter_version_options = ["M365","Pro","Essential","1s","Pro 2","Mi 3"]

    # Display available options
    for i, option in enumerate(scooter_version_options, 1):
        print(f"{i}. {option}")

    try:
        choice = int(input("\nSelect a scooter version (1-6): "))
        if 1 <= choice <= len(scooter_version_options):
            scooter_version = scooter_version_options[choice - 1]
        else:
            print("Please select a valid option.")
    except ValueError:
        print("Please enter a valid number.")

    print("Selected scooter version: " + scooter_version)
    open_firmware(scooter_version)
    # Menu header
    print("\n\n===== Select Firmware to Create =====")

    # Available firmware options
    firmware_options = ["Ransomware", "Undervoltage Attack", "Overvoltage Attack", "Denial of Service", "Data Integrity Attack", "Data Confidentiality Attack", "User Tracking Attack", "Data Leakage Attack"]

    # Display available options
    for i, option in enumerate(firmware_options, 1):
        print(f"{i}. {option}")

    # Ask the user to select an option
    try:
        choice = int(input("\nSelect a firmware (1-8): "))
        if 1 <= choice <= len(firmware_options):
            selected_firmware = firmware_options[choice - 1]
            create_firmware(selected_firmware, scooter_version)
        else:
            print("Please select a valid option.")
    except ValueError:
        print("Please enter a valid number.")

if __name__ == "__main__":
    main()
