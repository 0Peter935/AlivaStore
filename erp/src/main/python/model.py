
from flask import Flask, request, jsonify
import joblib
import numpy as np
from sklearn.linear_model import LinearRegression

app = Flask(__name__)

# Datos de ejemplo (luego se pueden cargar desde MySQL si quieres)
X = np.array([[1], [2], [3], [4]])
y = np.array([100, 150, 200, 250])

# Entrenar modelo
model = LinearRegression()
model.fit(X, y)

# Guardar modelo entrenado
joblib.dump(model, "modelo.pkl")

@app.route('/predict', methods=['POST'])
def predict():
    data = request.get_json()
    x_value = np.array([[data['x']]])
    prediction = model.predict(x_value)
    return jsonify({'prediction': float(prediction[0])})

if __name__ == '__main__':
    app.run(port=5000)
