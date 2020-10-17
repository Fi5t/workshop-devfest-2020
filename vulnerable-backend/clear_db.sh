rm -f db.sqlite3
rm -r todos/migrations
python manage.py makemigrations todos
python manage.py migrate