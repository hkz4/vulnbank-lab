FROM python:3.11-slim
WORKDIR /app
COPY . .
RUN pip install -r requirements.txt
RUN python init_db.py
EXPOSE 5000
CMD ["python", "app.py"]
