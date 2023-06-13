database_user="postgres"
database_name="schooldb"

psql -U $database_user -c "drop database $database_name"