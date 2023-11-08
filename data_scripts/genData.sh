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


generate_data() {
    ssh "$1" "~/gensort -a -b$2 $3 ~/data/partition.$4"
}

max_size=$((1*1024*1024*1024/100))  # 1 GB / 100b (100b size of a partition data file)
gen_size=$((100*1024*1024/100))      # 100 MB / 100b
max_nb_file=$((max_size/gen_size))
gen_step=0
# Boucle sur les adresses IP des workers
for ((i = 0; i < num_workers; i++)); do
    worker_ip="${ip_addresses[i]}"
    echo "Worker $i : $worker_ip"

    # Génération d'un nombre aléatoire de fichiers entre 1 et max_nb_file
    num_files=$((1 + RANDOM % (max_nb_file - 1)))

    for ((j = 1; j <= num_files; j++)); do
        # Utilisation de ssh pour exécuter gensort sur la machine distante
        (generate_data "$worker_ip" "$gen_step" "$gen_size" "$j" &)
        #ssh "$worker_ip" "~/gensort -a -b$gen_step $gen_size ~/data/partition.$i"
        gen_step=$((gen_step+gen_size))
    done
    echo "Fichier généré sur $ip : ~/data/partition.X => X = 1 to $num_files"
done

wait