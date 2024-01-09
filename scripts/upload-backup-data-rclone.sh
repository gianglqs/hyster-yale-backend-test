# move into /backup-data
cd /backup-data

filename=hysteryale_$(date +%Y-%m-%d_%H-%M).sql

# dump db
pg_password=postgres

PGPASSWORD=$pg_password pg_dump -U postgres -h localhost -d hysteryale > ${filename}


### upload file to folder backup_data on driver

rclone=/usr/bin/rclone
remote=test2

#upload file
$rclone copy $filename $remote:

### delete old data in local
prefix=hysteryale
files_sorted=($(ls -1t $prefix* 2>/dev/null))
max_file=7
if [ ${#files_sorted[@]} -gt $max_file ]; then
	files_to_delete=("${files_sorted[@]:$max_file}")
	for file in "${files_to_delete[@]}"; do
		rm -f "$file"
	done
fi
### delete old file in  gdrive
IFS=$'\n' gdrive_files_sorted=($($rclone ls $remote: | sort -k 2 -r | awk '{print $2}'))
if [ ${#gdrive_files_sorted[@]} -gt $max_file ]; then
        files_to_delete=("${gdrive_files_sorted[@]:$max_file}")
	echo ${files_to_delete}
        for file_id in "${files_to_delete}"; do
                $rclone delete $remote:"$file_id"
                echo "remove from Google Drive: $file_id"
        done
fi