package com.covision.covisionapp.structures

import java.util.ArrayList

class ObjectDetectionResult {
    var resultText: String
    var boxes: ArrayList<BoundingBox>

    init {
        this.resultText = ""
        boxes = ArrayList()
    }

    fun addText(text: String) {
        resultText = text
    }

    fun addBox(newBox: BoundingBox) {
        boxes.add(newBox)
    }
}
