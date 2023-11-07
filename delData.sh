#!/bin/bash

# Vérification des arguments
if [ $# -ne 1 ]; then
    echo "Usage: $0 <num_workers>"
    exit 1
fi

num_workers=$1

# Vérification de la plage de num_workers
if [ "$num_workers" -lt 1 ] || [ "$num_workers" -gt 9 ]; then
    echo "Le nombre de travailleurs (num_workers) doit être compris entre 1 et 9."
    exit 1
fi

ip_addresses=("2.2.2.143" "2.2.2.144" "2.2.2.145" 
                "2.2.2.146" "2.2.2.147" "2.2.2.148" 
                "2.2.2.149" "2.2.2.150" "2.2.2.151")

# Fonction pour générer un fichier de données sur une machine distante
delete_data() {
    local ip="$1"
    ssh "$ip" "rm ~/data/*"
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
    (delete_data "$worker_ip" &)
done

wait
