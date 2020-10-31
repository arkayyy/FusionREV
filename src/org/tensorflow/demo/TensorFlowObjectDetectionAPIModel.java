package org.tensorflow.demo;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.demo.env.Logger;


public class TensorFlowObjectDetectionAPIModel implements Classifier {
  private static final Logger LOGGER = new Logger();


  private static final int MAX_RESULTS = 100;

 
  private String inputName;
  private int inputSize;

 
  private Vector<String> labels = new Vector<String>();
  private int[] intValues; //templates for digits detection
  private byte[] byteValues;//templates for alphabets detection
  private float[] outputLocations;//to store wherever boxes are found
  private float[] outputScores;//to store the score of suxh detected boxes
  private float[] outputClasses;
  private float[] outputNumDetections;//to store all the detected numbers
  private String[] outputNames;//to store detected letters

  private boolean logStats = false;

  private TensorFlowInferenceInterface inferenceInterface;

  /**
   * Initializes a native TensorFlow session for classifying images.
   *
   * @param assetManager The asset manager to be used to load assets.
   * @param modelFilename The filepath of the model GraphDef protocol buffer.
   */
  public static Classifier create(
      final AssetManager assetManager,
      final String modelFilename,
      final int inputSize) throws IOException {
    final TensorFlowObjectDetectionAPIModel d = new TensorFlowObjectDetectionAPIModel();

    //InputStream labelsInput = null;
    //String actualFilename = labelFilename.split("file:///android_asset/")[1];
    //labelsInput = assetManager.open(actualFilename);
    //BufferedReader br = null;
    //br = new BufferedReader(new InputStreamReader(labelsInput));
    //String line;
    //while ((line = br.readLine()) != null) {
    //  LOGGER.w(line);
     // d.labels.add(line);
    //}
    //br.close();


    d.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);

    final Graph g = d.inferenceInterface.graph();

    d.inputName = "image_tensor";
    
    final Operation inputOp = g.operation(d.inputName);
    if (inputOp == null) {
      throw new RuntimeException("Failed to find input Node '" + d.inputName + "'");
    }
    d.inputSize = inputSize;
    
    final Operation outputOp1 = g.operation("detection_scores");
    if (outputOp1 == null) {
      throw new RuntimeException("Failed to find output Node 'detection_scores'");
    }
    final Operation outputOp2 = g.operation("detection_boxes");
    if (outputOp2 == null) {
      throw new RuntimeException("Failed to find output Node 'detection_boxes'");
    }
    final Operation outputOp3 = g.operation("detection_classes");
    if (outputOp3 == null) {
      throw new RuntimeException("Failed to find output Node 'detection_classes'");
    }


    d.outputNames = new String[] {"detection_boxes", "detection_scores",
                                  "detection_classes", "num_detections"};
    d.intValues = new int[d.inputSize * d.inputSize];
    d.byteValues = new byte[d.inputSize * d.inputSize * 3];
    d.outputScores = new float[MAX_RESULTS];
    d.outputLocations = new float[MAX_RESULTS * 4];
    d.outputClasses = new float[MAX_RESULTS];
    d.outputNumDetections = new float[1];
    return d;
  }

  private TensorFlowObjectDetectionAPIModel() {}

  public List<Recognition> recognizeImage(final Bitmap bitmap) {
    Trace.beginSection("recognizeImage");

    Trace.beginSection("preprocessBitmap");
    // Preprocess the image data from 0-255 int to normalized float based
    // on the provided parameters.
    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

    for (int i = 0; i < intValues.length; ++i) {
      byteValues[i * 3 + 2] = (byte) (intValues[i] & 0xFF);
      byteValues[i * 3 + 1] = (byte) ((intValues[i] >> 8) & 0xFF);
      byteValues[i * 3 + 0] = (byte) ((intValues[i] >> 16) & 0xFF);
    }
    Trace.endSection(); // preprocessBitmap

    // Copy the input data into TensorFlow.
    Trace.beginSection("feed");
    inferenceInterface.feed(inputName, byteValues, 1, inputSize, inputSize, 3);
    Trace.endSection();

    // Run the inference call.
    Trace.beginSection("run");
    inferenceInterface.run(outputNames, logStats);
    Trace.endSection();

    // Copy the output Tensor back into the output array.
    Trace.beginSection("fetch");
    outputLocations = new float[MAX_RESULTS * 4];
    outputScores = new float[MAX_RESULTS];
    outputClasses = new float [MAX_RESULTS];
    outputNumDetections = new float[1];
    //Float[][][] mb = new Float[1][100][100];
    inferenceInterface.fetch(outputNames[0], outputLocations);
    inferenceInterface.fetch(outputNames[1], outputScores);
    inferenceInterface.fetch(outputNames[2], outputClasses);
    inferenceInterface.fetch(outputNames[3], outputNumDetections);
    Trace.endSection();

    // Find the best detections.
    final PriorityQueue<Recognition> pq =
        new PriorityQueue<Recognition>(
            1,
            new Comparator<Recognition>() {
              @Override
              public int compare(final Recognition lhs, final Recognition rhs) {
             
                return Float.compare(rhs.getScore(), lhs.getScore());
              }
            });


    for (int i = 0; i < outputScores.length; ++i) {
      final RectF detection =
          new RectF(
              outputLocations[4 * i + 1] * inputSize,
              outputLocations[4 * i] * inputSize,
              outputLocations[4 * i + 3] * inputSize,
              outputLocations[4 * i + 2] * inputSize);
      pq.add(
          new Recognition(detection, outputScores[i], outputClasses[i]));
    }

    final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
    for (int i = 0; i < Math.min(pq.size(), MAX_RESULTS); ++i) {
      recognitions.add(pq.poll());
    }
    Trace.endSection(); // "recognizeImage"
    return recognitions;
  }

  @Override
  public void enableStatLogging(final boolean logStats) {
    this.logStats = logStats;
  }

  @Override
  public String getStatString() {
    return inferenceInterface.getStatString();
  }

  @Override
  public void close() {
    inferenceInterface.close();
  }
}
