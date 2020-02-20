import cv2
import time
import numpy as np

coco_classes = ['Ling-Angry', 'Ling-Cry', 'Ling-Decline', 'Ling-Get', 'Ling-Love', 'Ling-Me', 'Ling-Sad',
                'Ling-Shy', 'Ling-Smile', 'Ling-Walk', 'Yak-Angry', 'Yak-Cry',
                'Yak-Decline', 'Yak-Get', 'Yak-Love', 'Yak-Me', 'Yak-Sad', 'Yak-Shy', 'Yak-Smile',
                'Yak-Walk', 'Phra-Angry', 'Phra-Cry', 'Phra-Decline', 'Phra-Get', 'Phra-Love', 'Phra-Me',
                'Phra-Sad', 'Phra-Shy', 'Phra-Smile', 'Phra-Walk', 'Nang-Angry', 'Nang-Cry', 'Nang-Decline',
                'Nang-Get', 'Nang-Love', 'Nang-Me', 'Nang-Sad', 'Nang-Shy',
                'Nang-Smile', 'Nang-Wallk']


def process_image(img):
    """Resize, reduce and expand image.

    # Argument:
        img: original image.

    # Returns              
        image: ndarray(64, 64, 3), processed image.(1,416,416,3)
    """
    image = cv2.resize(img, (416, 416),
                       interpolation=cv2.INTER_CUBIC)
    image = np.array(image, dtype='float32')
    image /= 255.
    image = np.expand_dims(image, axis=0)
    print(image.shape)
    return image

def draw(image, boxes, scores, classes):
    """Draw the boxes on the image.

    # Argument:
        image: original image.
        boxes: ndarray, boxes of objects.
        classes: ndarray, classes of objects.
        scores: ndarray, scores of objects.
        all_classes: all classes name.
    """
    out_api = []
    for box, score, cl in zip(boxes, scores, classes):
        x, y, w, h = box

        # Rounding off to the nearest pixel for drawing box
        top = max(0, np.floor(x + 0.5).astype(int))
        left = max(0, np.floor(y + 0.5).astype(int))
        right = min(image.shape[1], np.floor(x + w + 0.5).astype(int))
        bottom = min(image.shape[0], np.floor(y + h + 0.5).astype(int))

        cv2.rectangle(image, (top, left), (right, bottom), (255, 0, 0), 2)
        cv2.putText(image, '{0} {1:.2f}'.format(coco_classes[cl], score),
                    (top, left - 6),
                    cv2.FONT_HERSHEY_SIMPLEX,
                    0.6, (0, 0, 255), 1,
                    cv2.LINE_AA)
        out_api.append((coco_classes[cl], score, box))

    return image, out_api



def detect_image(image, yolo):
    """Use yolo v3 to detect images.

    # Argument:
        image: original image.
        yolo: YOLO, yolo model.
        all_classes: all classes name.

    # Returns:
        image: processed image.
    """

    pimage = process_image(image)

    start = time.time()
    boxes, classes, scores = yolo.predict(pimage, image.shape)
    end = time.time()

    print('time: {0:.2f}s'.format(end - start)) 

    if boxes is not None:
        return draw(image, boxes, scores, classes)
    else:
        False
