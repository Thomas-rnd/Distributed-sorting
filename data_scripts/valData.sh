#!/bin/bash

# Function to check if the ~/data/output directory exists
check_output_directory() {
    ssh "$1" "[ -d $2 ]"
}

# Function to validate and concatenate summary files
validate_and_concatenate() {
    local worker_ip=$1
    local num_files=$2
    local out_index=$3
    local output_folder=$4
    local path_to_data=$5
    local valsort_path=$6

    for ((file_number = 1; file_number <= num_files; file_number++)); do
        # Validate each output file and create a summary file
        ssh "$worker_ip" "$valsort_path -q -o $path_to_data/out$file_number.sum $path_to_data/partition.$file_number"

        # Determine the output directory format based on out_index value
        if [ $out_index -lt 10 ]; then
            output_dir="out0$out_index.sum"
        else
            output_dir="out$out_index.sum"
        fi

        # Copy the summary file to the local machine with the determined directory name
        scp "$worker_ip:$path_to_data/out$file_number.sum" "$output_folder/$output_dir"
        
        ((out_index++))
    done
}

# Check the number of arguments
if [ $# -ne 4 ]; then
    echo "Usage: $0 <num_workers> <path_to_data> <valsort_path> <ip_list_txt>"
    exit 1
fi

num_workers=$1
path_to_data=$2
valsort_path=$3
ip_list_txt=$4

# Read IP addresses from the text file
ip_addresses=($(cat "$ip_list_txt"))

output_folder="/tmp/sortnet_OUTPUT"

# Check if the folder exists
if [ ! -d "$output_folder" ]; then
    # Create the folder if it doesn't exist
    mkdir -p "$output_folder"
else
    rm $output_folder/*
fi

# Check the ~/data/output directory on each remote machine
for ((i = 0; i < num_workers; i++)); do
    worker_ip="${ip_addresses[i]}"
    echo "Checking the path_to_data = $path_to_data directory on $worker_ip"

    if ! check_output_directory "$worker_ip" "$path_to_data"; then
        echo "Error: The path_to_data = $path_to_data directory on $worker_ip does not exist."
        exit 1
    fi
done

out_index=1
# Validate and concatenate summary files for each worker
for ((i = 0; i < num_workers; i++)); do
    worker_ip="${ip_addresses[i]}"
    echo "Validating and concatenating summary files on $worker_ip"

    # Assuming partition files are named partition.1, partition.2, ...
    num_files=$(ssh "$worker_ip" "ls -l $path_to_data/partition.* | wc -l")

    validate_and_concatenate "$worker_ip" "$num_files" "$out_index" "$output_folder" "$path_to_data" "$valsort_path"
    out_index=$((out_index + num_files))
done

# Concatenate all summary files into all_final.sum
echo "Concatenating all summary files into all_final.sum"
cat $output_folder/*.sum > $output_folder/all_final.sum

# Validate the final summary file
echo "Validating the final summary file"

$valsort_path -s $output_folder/all_final.sum

rm $output_folder/*.sum

for ((i = 0; i < num_workers; i++)); do
    worker_ip="${ip_addresses[i]}"
    ssh "$worker_ip" "rm $path_to_data/*.sum"

done

echo "Validation completed successfully."
