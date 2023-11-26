#!/bin/bash

# Vérification des arguments
if [ $# -ne 3 ]; then
    echo "Usage: $0 <num_workers> <data_folder_path> <ip_list_txt>"
    exit 1
fi

num_workers=$1
data_folder_path=$2
ip_list_txt=$3

# Read IP addresses from the text file
ip_addresses=($(cat "$ip_list_txt"))

# Check the range of num_workers
if [ "$num_workers" -lt 1 ] || [ "$num_workers" -gt "${#ip_addresses[@]}" ]; then
    echo "(num_workers) should be between 1 and the number of IP addresses in the file."
    exit 1
fi


# Fonction pour générer un fichier de données sur une machine distante
delete_data() {
    local ip="$1"
    ssh "$ip" "rm -rf $2/*/*"
    status=$?
    if [ $status -eq 0 ]; then
        echo "Data delete on $ip"
    else
        echo "Data delete on $ip failed with exit code $status"
    fi
}

# Boucle sur les adresses IP des workers
for ((i = 0; i < num_workers; i++)); do
    worker_ip="${ip_addresses[i]}"
    echo "Travailleur $i : $worker_ip"
    
    # Exécution de la fonction de génération de données sur le travailleur actuel
    (delete_data "$worker_ip" "$data_folder_path"&)
done

wait
