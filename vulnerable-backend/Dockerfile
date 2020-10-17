FROM python:3.8-buster

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

RUN mkdir /opt/backend
WORKDIR /opt/backend

COPY Pipfile Pipfile.lock /opt/backend/
RUN pip install pipenv && pipenv install --system

COPY . /opt/backend/

CMD ./manage.py collectstatic --noinput && ./manage.py migrate && \
    gunicorn --bind 0.0.0.0:$PORT --workers=2 --access-logfile - vulnerable_backend.wsgi:application
