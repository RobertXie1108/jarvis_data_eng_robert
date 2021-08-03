#! /bin/bash

# validate number of arguments
if [[ $# -ne 5 ]]; then
	echo "Incorrect number of arguments
Usage: host_info.sh db-hostname db-port db-username db-password host_agent"
	exit 1
fi

#collect arguments
db_host=$1
db_port=$2
db_uid=$3
db_pass=$4
db_name=$5

#Collect host hardware information
lscpu_out=$(lscpu)
cpu_count=$(echo "$lscpu_out" | grep "^CPU(s):" | awk '{ print $2 }')
cpu_arch=$(echo "$lscpu_out" | grep "^Arch" | awk '{ print $2 }')
cpu_mdl=$(echo "$lscpu_out" | grep "^Model name" | awk '{ $1=$2="";  print }' | sed 's/^[[:space:]]*//')
cpu_mhz=$(echo "$lscpu_out" | grep "^CPU MHz:" | awk '{ print $3 }')
l2_cache=$(echo "$lscpu_out" | grep "^L2 cache:" | awk '{ print $3 }' | sed 's/K//')
mem_total=$(grep "^MemTotal" /proc/meminfo | awk '{ print $2 }')
hostname=$(hostname -f)
curr_time=$(date -u +"%Y-%m-%d %H:%M:%S")

#Export postgres user's password
export PGPASS="$db_pass"

#Execute an upsert using the supplied heredoc
psql -h $db_host -p $db_port -U $db_uid -w -d $db_name << EOF
insert into host_info (
	hostname, cpu_count, cpu_arch, cpu_model,
	cpu_mhz, l2_cache, mem_total, curr_time
) 
values 
	(
		'$hostname', $cpu_count, '$cpu_arch', 
		'$cpu_mdl', $cpu_mhz, $l2_cache, 
		$mem_total, '$curr_time'
	) on conflict (hostname) do 
update 
set 
	cpu_count = excluded.cpu_count, 
	cpu_arch = excluded.cpu_arch,
	cpu_model = excluded.cpu_model,
	cpu_mhz = excluded.cpu_mhz,
	l2_cache = excluded.l2_cache,
	mem_total = excluded.mem_total,
	curr_time = excluded.curr_time;
EOF

if [[ $? -ne 0 ]]; then
	echo "The SQL query did not complete properly"
	exit 2
fi

exit 0
