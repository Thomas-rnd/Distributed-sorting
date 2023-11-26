#!/bin/bash

# Function to check if the ~/data/output directory exists
check_output_directory() {
    ssh "$1" '[ -d ~/data/output ]'
}

# Function to validate and concatenate summary files
validate_and_concatenate() {
    local worker_ip=$1
    local num_files=$2

    for ((file_number = 1; file_number <= num_files; file_number++)); do
        # Validate each output file and create a summary file
        ssh "$worker_ip" "valsort -o ~/data/output/out$file_number.sum ~/data/output/partition.$file_number"

        # Copy the summary file to the local machine with a unique name
        scp "$worker_ip:~/data/output/out$file_number.sum" "./all_$worker_ip_$file_number.sum"
    done
}

# Check the number of arguments
if [ $# -ne 1 ]; then
    echo "Usage: $0 <num_workers>"
    exit 1
fi

num_workers=$1

ip_addresses=("2.2.2.143" "2.2.2.144" "2.2.2.145" 
                "2.2.2.146" "2.2.2.147" "2.2.2.148" 
                "2.2.2.149" "2.2.2.150" "2.2.2.151")

# Check the ~/data/output directory on each remote machine
for ((i = 0; i < num_workers; i++)); do
    worker_ip="${ip_addresses[i]}"
    echo "Checking the ~/data/output directory on $worker_ip"

    if ! check_output_directory "$worker_ip"; then
        echo "Error: The ~/data/output directory on $worker_ip does not exist."
        exit 1
    fi
done

# Validate and concatenate summary files for each worker
for ((i = 0; i < num_workers; i++)); do
    worker_ip="${ip_addresses[i]}"
    echo "Validating and concatenating summary files on $worker_ip"

    # Assuming partition files are named partition.1, partition.2, ...
    num_files=$(ssh "$worker_ip" "ls -l ~/data/output/partition.* | wc -l")

    validate_and_concatenate "$worker_ip" "$num_files"
done

# Concatenate all summary files into all_final.sum
echo "Concatenating all summary files into all_final.sum"
cat ./all_*.sum > all_final.sum

# Validate the final summary file
echo "Validating the final summary file"
valsort -s all_final.sum

echo "Validation completed successfully."
