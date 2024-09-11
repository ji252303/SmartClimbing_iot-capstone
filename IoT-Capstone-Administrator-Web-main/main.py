from flask import Flask, request, render_template, send_from_directory, url_for, redirect, jsonify
import os
from ultralytics import YOLO
import cv2
import numpy as np
from PIL import Image
from werkzeug.utils import secure_filename
import matplotlib.pyplot as plt
import requests
import boto3
import botocore

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = 'uploads/'
app.config['ALLOWED_EXTENSIONS'] = {'png', 'jpg', 'jpeg', 'gif'}
labels = ["black", "blue", "gray", "green", "human", "orange", "pink", "purple", "red", "white", "yellow"]
boxes_data = []
root = ""
bucket_name = 'imagebucketfornano'


# HTTP 요청을 보낼 URL
API_URL = 'https://sphizygxo4.execute-api.ap-northeast-2.amazonaws.com/prod/maps/1'

blue_BGR = (255, 0, 0)
marker_size = 35
marker_3d_edges = np.array([[0, 0, 0],
                            [0, marker_size, 0],
                            [marker_size, marker_size, 0],
                            [marker_size, 0, 0]], dtype='float32').reshape((4, 1, 3))

def process_bounding_boxes(image, boxes, labels):
    for box in boxes:
        x1, y1, x2, y2, label_idx = box  # 바운딩 박스 좌표와 라벨 인덱스를 추출
        label = labels[label_idx]  # 라벨 인덱스에 해당하는 라벨 이름 가져오기
        #if label not in selected_labels:
        #   continue
        box_corners = np.array([[x1, y1], [x2, y1], [x2, y2], [x1, y2]], dtype=np.float32)
        ret, rvec, tvec = cv2.solvePnP(marker_3d_edges, box_corners, cmtx, dist)
        if ret:
            x, y, z = [round(val[0], 2) for val in tvec]
            rx, ry, rz = [round(np.rad2deg(val[0]), 2) for val in rvec]
            topLeft = np.int32(box_corners[0])
            cv2.putText(image, f"{x},{y},{z}", tuple(topLeft + np.array([-10, 10])), cv2.FONT_HERSHEY_PLAIN, 2.0, (0, 0, 255), 2)
            cv2.putText(image, f"{rx},{ry},{rz}", tuple(topLeft + np.array([-10, 40])), cv2.FONT_HERSHEY_PLAIN, 2.0, (0, 0, 255), 2)
            cv2.putText(image, label, tuple(topLeft + np.array([-10, 70])), cv2.FONT_HERSHEY_PLAIN, 2.0, (0, 0, 255), 2)
            boxes_data.append({
                'x': x, 'y': y, 'z': z, 'rx': rx, 'ry': ry, 'rz': rz, 'label': label, 'uid': ''
            })
def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in app.config['ALLOWED_EXTENSIONS']


def load_model():
    return YOLO('./best_float32.tflite', task='detect')


@app.route('/save_selection', methods=['POST'])
def save_selection():
    selected_uids = request.form.getlist('selected_boxes')
    selected_boxes = [box for box in boxes_data if box['uid'] in selected_uids]
    # 이곳에서 선택된 박스를 저장하는 로직을 추가할 수 있습니다.
    print(f"Selected boxes: {selected_boxes}")
    return redirect(url_for('upload_file'))


@app.route('/', methods=['GET', 'POST'])
def upload_file():
    if request.method == 'POST':
        if 'file' not in request.files:
            return render_template('upload.html', message='No file uploaded.')
        file = request.files['file']
        if file.filename == '':
            return render_template('upload.html', message='No file selected.')
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
            file.save(file_path)
            
            # 이미지 처리 부분
            desired_label = request.form.get('desired_label')
            image = cv2.imread(file_path)
            processed_image_path, graph_image_path, boxes = process_image(image, desired_label)
            boxes_data = [{'box_id': i + 1, 'x': box[0], 'y': box[1], 'width': box[2], 'height': box[3], 'rx': np.rad2deg(box[2]), 'ry': np.rad2deg(box[3]), 'uid': ''} for i, (box, label) in enumerate(zip(boxes, labels))]
            
            return render_template('upload.html', message='Upload and processing successful.', processed_image_path=processed_image_path, graph_image_path=graph_image_path, labels=labels, boxes=boxes_data)
        else:
            return render_template('upload.html', message='Invalid file type.')
    return render_template('upload.html')

@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)

@app.route('/filter', methods=['POST'])
def filter_labels():
    global selected_labels
    selected_labels = request.form.getlist('labels')
    return redirect(url_for('index'))


def send_data_to_api(boxes, root):
    try:
        for box in boxes:
            data = {
                'root': root,
                'hold_numbering': box['hold_numbering'],
                'x': box['x'],
                'y': box['y'],
                'rx': box['rx'],
                'ry': box['ry'],
                'uid': box['uid']
            }
            response = requests.post(API_URL, json=data)
            response.raise_for_status()
        return "Data sent to API successfully!"
    except requests.exceptions.RequestException as e:
        print(f"Error sending data to API: {e}")
        return f"Failed to send data to API: {e} - {response.text}"



@app.route('/add_uid_hold', methods=['POST'])
def add_uid_hold():
    global boxes_data, root
    uids = request.form.getlist('uids')
    holds = request.form.getlist('holds')
    root = request.form.get('root', '')

    for i, (uid, hold) in enumerate(zip(uids, holds)):
        if 0 <= i < len(boxes_data):
            boxes_data[i]['uid'] = uid
            boxes_data[i]['hold_numbering'] = hold

    try:    

        # 파일 업로드 요청 생성
        with open(file_name, 'rb') as f:
            s3.upload_fileobj(f, bucket_name, file_obj_key_name)

        print("Upload complete.")

    except botocore.exceptions.ClientError as e:
        if e.response['Error']['Code'] == "404":
            print("The object does not exist.")
        else:
            raise

    message = send_data_to_api(boxes_data, root)

    return render_template('upload.html', processed_image_path='', graph_image_path='', boxes=boxes_data, root=root, message=message)

# 카메라 매트릭스와 왜곡 계수 초기화
cmtx = np.array([[1000, 0, 640], [0, 1000, 360], [0, 0, 1]], dtype=np.float32)
dist = np.array([0, 0, 0, 0, 0], dtype=np.float32)

def process_image(image, desired_label):
    model = load_model()
    results = model.predict(source=image, save=True, imgsz=416, conf=0.5)

    boxes = []
    box_id = 1
    for result in results:
        result_boxes = result.boxes
        result_names = result.names

        for box, label_idx in zip(result_boxes.xyxy.cpu().numpy(), result_boxes.cls.cpu().numpy().astype(int)):
            x1, y1, x2, y2 = map(int, box[:4])
            label = result_names[label_idx]

            # 원하는 레이블만 처리
            if label == desired_label:
                cv2.rectangle(image, (x1, y1), (x2, y2), (255, 0, 0), 2)
                cv2.putText(image, f"Box {box_id}", (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 0, 255), 2)

                boxes.append([x1, y1, x2, y2, label_idx])
                box_id += 1

    process_bounding_boxes(image, boxes, result_names)

    processed_image_path = os.path.join(app.config['UPLOAD_FOLDER'], 'processed_image.jpg')
    cv2.imwrite(processed_image_path, image)
    
    # 기본 설정
    canvas_height = 500
    canvas_width = 500
    scale_factor = 3  # 좌표 간 거리를 1.5배 늘림

    # 최대/최소 좌표를 저장할 변수 초기화
    max_x = -np.inf
    max_y = -np.inf
    min_x = np.inf
    min_y = np.inf
    

    scaled_points = []
    # 각 상자의 위치와 방향을 표시
    for box in boxes_data:
        x_scaled = int((box['x'] + canvas_width // 2) * scale_factor)
        y_scaled = int((box['y'] + canvas_height // 2) * scale_factor)
        scaled_points.append((x_scaled, y_scaled))
    
        max_x = max(max_x, x_scaled)
        max_y = max(max_y, y_scaled)
        min_x = min(min_x, x_scaled)
        min_y = min(min_y, y_scaled)

    # Define new canvas dimensions with some padding
    canvas_width = max_x - min_x + 400
    canvas_height = max_y - min_y + 400
    canvas = np.ones((canvas_height, canvas_width, 3), dtype=np.uint8) * 255

    # Compute center of all points and adjust to center them on the canvas
    center_x = (max_x + min_x) // 2
    center_y = (max_y + min_y) // 2
    canvas_center_x = canvas_width // 2
    canvas_center_y = canvas_height // 2

    # Adjust points to the new center
    for x, y in scaled_points:
        x_adj = x - center_x + canvas_center_x + 25  # Adjust and add padding
        y_adj = y - center_y + canvas_center_y + 25
        cv2.circle(canvas, (x_adj, y_adj), 20, (255, 0, 0), -1)

    graph_filename = 'graph.png'
    graph_image_path = os.path.join(app.config['UPLOAD_FOLDER'], graph_filename)
    cv2.imwrite(graph_image_path, canvas)

    # ArUco 마커 감지 설정
    board_type = cv2.aruco.DICT_6X6_250
    arucoDict = cv2.aruco.getPredefinedDictionary(board_type)
    parameters = cv2.aruco.DetectorParameters()
    dectector = cv2.aruco.ArucoDetector(arucoDict,parameters)
    corners, ids, rejectedCandidates = dectector.detectMarkers(image)

    if ids is not None:
        for i, corner in enumerate(corners):
            corner = corner.reshape((4, 2))
            (topLeft, topRight, bottomRight, bottomLeft) = corner
            ret, rvec, tvec = cv2.solvePnP(marker_3d_edges, corner, cmtx, dist)
            if ret:
                x, y, z = [round(val[0], 2) for val in tvec]
                rx,ry,rz = [round(np.rad2deg(val[0]), 2) for val in rvec]
                cv2.putText(image, f"X:{x}, Y:{y}, Z:{z}", tuple(topLeft.astype(int)), cv2.FONT_HERSHEY_PLAIN, 1,
                            (0, 255, 0), 2)
                cv2.putText(image, f"rX:{rx}, rY:{ry}, rZ:{rz}", tuple(topLeft.astype(int)), cv2.FONT_HERSHEY_PLAIN, 1,
                            (0, 255, 0), 2)

    return processed_image_path, graph_image_path, boxes


if __name__ == '__main__':
    app.run(debug=True)





