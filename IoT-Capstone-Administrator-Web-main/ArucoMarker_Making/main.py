import torch
import cv2
import numpy as np
import time
import os

os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"

if __name__ == "__main__":
    # 모델 로드
    model = torch.hub.load('ultralytics/yolov8', 'custom', path='/best.pt',trust_repo='check')
    # 카메라 생성 (웹캠 사용)
    cam = cv2.VideoCapture(0)  # 0은 대부분의 시스템에서 기본 웹캠을 가리킵니다.
    if not cam.isOpened():
        raise IOError("Cannot open webcam")

    # realsense 카메라 초기 노출시간 확보를 위한 대체 (약간의 딜레이 추가)
    time.sleep(2)

    cmtx = [[1000, 0, 640], [0, 1000, 360], [0, 0, 1]]  # fx, fy를 1000으로, cx, cy를 이미지의 중심으로 설정
    dist = [0, 0, 0, 0, 0]  # 왜곡 계수, 복잡한 캘리브레이션 없이 모두 0으로 설정

    # aruco detector 생성
    board_type = cv2.aruco.DICT_6X6_250
    arucoDict = cv2.aruco.getPredefinedDictionary(board_type)
    parameters = cv2.aruco.DetectorParameters()  # DetectorParameters() 대신 DetectorParameters_create() 사용
    detector = cv2.aruco.ArucoDetector(arucoDict, parameters)  # 이 부분은 OpenCV의 버전에 따라 달라질 수 있으니 확인 필요

    while True:
        # 웹캠으로부터 촬영 이미지 가져오기
        ret, img = cam.read()

        # 파란색상 정의
        blue_BGR = (255, 0, 0)


        marker_size = 35
        marker_3d_edges = np.array([[0, 0, 0],
                                    [0, marker_size, 0],
                                    [marker_size, marker_size, 0],
                                    [marker_size, 0, 0]], dtype='float32').reshape((4, 1, 3))

        if ret:
            # 마커(marker) 검출
            corners, ids, rejectedCandidates = detector.detectMarkers(img)

            # 검출된 마커들의 꼭지점을 이미지에 그려 확인
            if corners:  # corners가 비어있지 않은 경우에만 실행
                for corner in corners:
                    corner = np.array(corner).reshape((4, 2))
                    (topLeft, topRight, bottomRight, bottomLeft) = corner

                    topRightPoint = (int(topRight[0]), int(topRight[1]))
                    topLeftPoint = (int(topLeft[0]), int(topLeft[1]))
                    bottomRightPoint = (int(bottomRight[0]), int(bottomRight[1]))
                    bottomLeftPoint = (int(bottomLeft[0]), int(bottomLeft[1]))

                    cv2.circle(img, topLeftPoint, 4, blue_BGR, -1)
                    cv2.circle(img, topRightPoint, 4, blue_BGR, -1)
                    cv2.circle(img, bottomRightPoint, 4, blue_BGR, -1)
                    cv2.circle(img, bottomLeftPoint, 4, blue_BGR, -1)

                    ret, rvec, tvec = cv2.solvePnP(marker_3d_edges, corner, np.array(cmtx), np.array(dist))
                    if (ret):
                        x = round(tvec[0][0], 2);
                        y = round(tvec[1][0], 2);
                        z = round(tvec[2][0], 2);
                        rx = round(np.rad2deg(rvec[0][0]), 2);
                        ry = round(np.rad2deg(rvec[1][0]), 2);
                        rz = round(np.rad2deg(rvec[2][0]), 2);
                        # PnP 결과를 이미지에 그려 확인
                        text1 = f"{x},{y},{z}"
                        text2 = f"{rx},{ry},{rz}"
                        cv2.putText(img, text1, (int(topLeft[0] - 10), int(topLeft[1] + 10)), cv2.FONT_HERSHEY_PLAIN,
                                    1.0, (0, 0, 255))
                        cv2.putText(img, text2, (int(topLeft[0] - 10), int(topLeft[1] + 40)), cv2.FONT_HERSHEY_PLAIN,
                                    1.0, (0, 0, 255))

            cv2.imshow("Frame", img)
            key = cv2.waitKey(1) & 0xFF

            # 'q'를 누르면 루프에서 탈출
            if key == ord('q'):
                break

    cam.release()
    cv2.destroyAllWindows()





