database_user="postgres"
database_name="schooldb"

create_database="create database schooldb"

students_insert=$(cat <<EOF
drop table if exists students;

create table if not exists students (
    id serial primary key not null,
    name text not null,
    class text not null,
    class_teacher_id integer,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists students_name_class_idx on students(name, class);

EOF
)

teachers_insert=$(cat <<EOF
drop table if exists teachers;

create table if not exists teachers (
    id serial not null primary key,
    name text not null,
    subject text not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists teachers_name_subject_idx on teachers(name, subject);

EOF
)

student_teachers_association_insert=$(cat <<EOF
drop table if exists student_teachers_association;

create table if not exists student_teachers_association (
    id serial not null primary key,
    student_id text not null,
    teacher_id jsonb not null
);

EOF
)

create_set_updated_at_trigger_function=$(cat <<EOF
create or replace function set_updated_at_trigger_function() 
returns trigger as \$\$
    BEGIN
        new.updated_at = now()::timestamptz;
        return new;
    END;
\$\$ LANGUAGE plpgsql;

EOF
)

create_triggers=$(cat <<EOF
create trigger students_updated_at_trigger
BEFORE UPDATE on students
FOR EACH ROW 
EXECUTE function set_updated_at_trigger_function();

create trigger teachers_updated_at_trigger
BEFORE UPDATE on teachers
FOR EACH ROW 
EXECUTE function set_updated_at_trigger_function();

EOF
)

sql_commands="$students_insert $teachers_insert $student_teachers_association_insert $create_set_updated_at_trigger_function $create_triggers"

psql -U $database_user -c "$create_database"

psql -U $database_user -d $database_name -c "$sql_commands"