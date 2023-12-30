#!/bin/bash
# Folder local
folder_local="/data-backup-local"

# ID folder ggdrive
drive_folder_id="1eZB4Zn4NoY9xqes5b4NcFWwrqX3eAd7m"

# get tool
gdrive="/home/linuxbrew/.linuxbrew/bin/gdrive"

# move to work directory
cd $folder_local

# get list file on ggdrive
IFS=$'\n' gdrive_files=$($gdrive files list --parent "$drive_folder_id" --skip-header)

for file in ${gdrive_files[@]}; do

    file_id=$(echo $file | awk '{print $1}')
    file_name=$(echo $file | awk '{print $2}')
    local_file_path="$folder_local/$file_name"

    if [ ! -e "$local_file_path" ]; then
        $gdrive files download "$file_id"
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