#!/bin/bash

# Function to check if the ~/data/input directory exists and is empty
check_input_directory() {
    ssh "$1" '[ -d ~/data/input ] && [ -z "$(ls -A ~/data/input)" ]'
}

# Function to generate ascii data on a remote machine
generate_ascii_data() {
    ssh "$1" "$gensort_path -a -b$2 $3 ~/data/input/folder_$6/partition$7"
}

# Function to generate byte data on a remote machine
generate_byte_data() {
    ssh "$1" "$gensort_path -b$2 $3 ~/data/input/folder_$6/partition$7"
}

# Function to generate data on a remote machine
generate_data() {
    local worker_ip=$1
    local gen_step=$2
    local gen_size=$3
    local data_type=$4
    local gensort_path=$5
    local folder_number=$6
    local file_number=$7

    if [ "$data_type" == "ascii" ]; then
        generate_ascii_data "$worker_ip" "$gen_step" "$gen_size" "$data_type" "$gensort_path" "$folder_number" "$file_number"
    elif [ "$data_type" == "byte" ]; then
        generate_byte_data "$worker_ip" "$gen_step" "$gen_size" "$data_type" "$gensort_path" "$folder_number" "$file_number"
    fi
}




# Check the number of arguments
if [ $# -lt 3 ]; then
    echo "Usage: $0 <num_workers> <data_type> <gensort_path> [-mf]"
    echo "  data_type: 'ascii' or 'byte'"
    echo "  gensort_path: Path to the gensort script on remote machines (e.g., '~/gensort')"
    echo "  -mf: Optional flag to generate data in multiple folders"
    exit 1
fi

num_workers=$1
data_type=$2
gensort_path=$3
multiple_folders=false

# Check the optional flag for multiple folders
if [ "$4" == "-mf" ]; then
    multiple_folders=true
fi

# Check the range of num_workers
if [ "$num_workers" -lt 1 ] || [ "$num_workers" -gt 9 ]; then
    echo "Error: The number of workers (num_workers) must be between 1 and 9."
    exit 1
fi

# Check if data_type is either 'ascii' or 'byte'
if [ "$data_type" != "ascii" ] && [ "$data_type" != "byte" ]; then
    echo "Error: Invalid data_type. Use 'ascii' or 'byte'."
    exit 1
fi

ip_addresses=("2.2.2.143" "2.2.2.144" "2.2.2.145" 
                "2.2.2.146" "2.2.2.147" "2.2.2.148" 
                "2.2.2.149" "2.2.2.150" "2.2.2.151")


# Check the ~/data/input directory on each remote machine
for ((i = 0; i < num_workers; i++)); do
    worker_ip="${ip_addresses[i]}"
    echo "Checking the ~/data/input directory on $worker_ip"
    
    if ! check_input_directory "$worker_ip"; then
        echo "Error: The ~/data/input directory on $worker_ip must be empty at the beginning of the script."
        exit 1
    fi
done

# Calculate the maximum size in 100-byte blocks for 100 MB
max_size=$((100 * 1024 * 1024 / 100))

# Calculate the size in 100-byte blocks for 10 MB
gen_size=$((10 * 1024 * 1024 / 100))

max_nb_file=$((max_size/gen_size))
gen_step=0



for ((i = 0; i < num_workers; i++)); do
    worker_ip="${ip_addresses[i]}"
    echo "Worker $i : $worker_ip"

    
    if $multiple_folders; then
        num_folders=$((RANDOM % 3 + 1))
    else
        num_folders=1
    fi

    for ((folder_number = 1; folder_number <= num_folders; folder_number++)); do
        ssh "$worker_ip" "mkdir ~/data/input/folder_$folder_number"
        num_files=$((1 + RANDOM % (max_nb_file - 1)))
        for ((file_number = 1; file_number <= num_files; file_number++)); do
            generate_data "$worker_ip" "$gen_step" "$gen_size" "$data_type" "$gensort_path" "$folder_number" "$file_number" &
            gen_step=$((gen_step + gen_size))
        done
        echo "Files generated on $worker_ip : ~/data/input/folder_$folder_number/partition.X => X = 1 to $num_files"
    done
    
done

wait
