#! /bin/bash
#This script is for collecting host resource useage information
#and updating that information in the DB

#validate number of arguments
if [[ $# -ne 4 ]]; then
	echo "Invalid number of arguments,\
Usage: host_usage.sh db-hostname db-port db-username db-name"
	exit 1
fi

#assign CLI args and hostname
db_host=$1
db_port=$2
db_uid=$3
db_name=$4
hostname=$(hostname -f)

#Start collecting useage info, free disk space comes from df
#All other info comes from vmstat. The first line is always average since boot
#So vmstat must run for a minute, then we get the tail
disk_free=$(df -BM / | tail -1 | awk '{ print $4  }' | sed 's/M//')
sys_avg=$(vmstat -SM 60 2 | tail -1)

#Extract usage info from vmstat output
mem_free=$(echo "$sys_avg" | awk '{ print $4 }')
disk_i=$(echo "$sys_avg" | awk '{ print $9 }')
disk_o=$(echo "$sys_avg" | awk '{ print $10 }')
disk_io=$(($disk_i+$disk_o))
cpu_kern=$(echo "$sys_avg" | awk '{ print $14 }')
cpu_idle=$(echo "$sys_avg" | awk '{ print $15 }')
curr_time=$(date -u +"%Y-%m-%d %H:%M:%S")

#Enter gathered data into the DB. The query is messy, but we don't store the
#Host's ID anywhere so we have to pull it live
psql -h $db_host -p $db_port -U $db_uid -d $db_name -w << EOF
insert into host_usage
values 
(
	(
		select 
			id 
		from 
			host_info 
		where 
			hostname = '$hostname'
	), 
	$mem_free, 
	$cpu_idle, 
	$cpu_kern, 
	$disk_io, 
	$disk_free, 
	'$curr_time'
) on conflict (id) do 
update
set
	 memory_free = excluded.memory_free,
	cpu_idle = excluded.cpu_idle,
	cpu_kernel = excluded.cpu_kernel,
	disk_io = excluded.disk_io,
	disk_avail = excluded.disk_avail,
	curr_time = excluded.curr_time;
EOF

if [[ $? -ne 0 ]]; then
	echo "The DB query did not execute properly"
	exit 2
fi
exit 0
