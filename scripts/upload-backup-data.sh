# move into /backup-data
cd /backup-data
filename=hysteryale_$(date +%Y-%m-%d_%H:%M).sql
# dump db
pg_password=postgres

PGPASSWORD=$pg_password pg_dump -U postgres -h localhost -d hysteryale > ${filename}
### upload file to folder backup_data on driver
# id_folder
gdrive_folder_id=1eZB4Zn4NoY9xqes5b4NcFWwrqX3eAd7m
# bash
gdrive=/home/linuxbrew/.linuxbrew/bin/gdrive
#/home/oem/.google-drive-upload/bin/gupload ${filename} -c backup_data >> log.txt
$gdrive files upload --parent $gdrive_folder_id $filename
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
#gdrive_files_sorted=($(gdrive files list --parent "$gdrive_folder_id" --skip-header --order-by "modifiedTime desc" | awk '{print $1}'))
IFS=$'\n' gdrive_files_sorted=($(gdrive files list --parent "$gdrive_folder_id" --skip-header | sort -k 2 -r | awk '{print $1}'))
if [ ${#gdrive_files_sorted[@]} -gt $max_file ]; then
        files_to_delete=("${gdrive_files_sorted[@]:$max_file}")
	echo ${gdrive_files_sorted[@]}
	echo ${files_to_delete}
        for file_id in "${files_to_delete}"; do
                $gdrive files delete "$file_id"
                echo "remove from Google Drive: $file_id"
        done
fi