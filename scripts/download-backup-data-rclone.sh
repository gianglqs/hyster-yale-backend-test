#!/bin/bash
# Folder local
folder_local="/home/oem/backup-data-local"

# get tool
rclone=/usr/bin/rclone
remote=test2
# move to work directory
cd $folder_local

# get list file on ggdrive
IFS=$'\n' gdrive_files=$($rclone ls $remote:)

for file in ${gdrive_files[@]}; do

    file_name=$(echo $file | awk '{print $2}')
    local_file_path="$folder_local/$file_name"

    if [ ! -e "$local_file_path" ]; then
        $rclone copy $remote:"$file_name" $folder_local
    fi

done

### delete old data in local

prefix=hysteryale
files_sorted=($(ls -1r $prefix* 2>/dev/null)) #sort file by time desc
max_file=7

if [ ${#files_sorted[@]} -gt $max_file ]; then
        files_to_delete=("${files_sorted[@]:$max_file}")
        for file in "${files_to_delete[@]}"; do
        	echo "remove file: $file"
                rm -f "$file"
        done
fi