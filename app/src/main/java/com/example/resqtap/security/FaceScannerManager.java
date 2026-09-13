package com.example.resqtap.security;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.resqtap.R;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.Collections;
import java.util.List;

/**
 * Handles real-time camera preview and genuine facial detection for Face ID authentication.
 * Uses Camera2 API for camera feed and Google ML Kit Face Detection to ensure
 * authentication only succeeds when a genuine human face is actively detected.
 */
public class FaceScannerManager {
    private static final String TAG = "FaceScannerManager";
    private static final long SCAN_TIMEOUT_MS = 25000L; // 25 seconds timeout

    public interface FaceScanListener {
        void onCameraReady();
        void onFaceScanning(boolean faceDetected, String statusMessage);
        void onHeadTurnProgress(float leftProgress, float rightProgress, String prompt);
        void onFaceVerified(Face face);
        void onError(String errorMessage);
        void onTimeout();
    }

    private final Context context;
    private final TextureView textureView;
    private final FaceScanListener listener;
    private final Handler mainHandler;

    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private HandlerThread cameraThread;
    private Handler cameraHandler;
    private FaceDetector faceDetector;

    private volatile boolean isScanning = false;
    private volatile boolean isAnalyzing = false;
    private long scanStartTime = 0L;
    private int consecutiveDetectedCount = 0;

    // Head turn tracking (0.0f to 1.0f)
    private float leftTurnProgress = 0f;
    private float rightTurnProgress = 0f;
    private boolean isTurnHeadMode = true;

    public void setTurnHeadMode(boolean turnHeadMode) {
        this.isTurnHeadMode = turnHeadMode;
    }

    public boolean isTurnHeadMode() {
        return isTurnHeadMode;
    }

    public FaceScannerManager(@NonNull Context context,
                              @NonNull TextureView textureView,
                              @NonNull FaceScanListener listener) {
        this.context = context.getApplicationContext();
        this.textureView = textureView;
        this.listener = listener;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Start camera preview and real face scanning.
     */
    public synchronized void startScanning() {
        if (isScanning) return;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            mainHandler.post(() -> listener.onError(context.getString(R.string.face_id_camera_error)));
            return;
        }

        isScanning = true;
        isAnalyzing = false;
        consecutiveDetectedCount = 0;
        leftTurnProgress = 0f;
        rightTurnProgress = 0f;
        scanStartTime = SystemClock.elapsedRealtime();

        startBackgroundThread();

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .setMinFaceSize(0.18f)
                .build();
        faceDetector = FaceDetection.getClient(options);

        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                    if (isScanning) {
                        openCamera();
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                }
            });
        }
    }

    private void startBackgroundThread() {
        if (cameraThread != null) return;
        cameraThread = new HandlerThread("FaceScannerBackground");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (cameraThread != null) {
            cameraThread.quitSafely();
            try {
                cameraThread.join(500);
            } catch (InterruptedException ignored) {
            }
            cameraThread = null;
            cameraHandler = null;
        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        if (!isScanning) return;
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) {
            mainHandler.post(() -> listener.onError(context.getString(R.string.face_id_camera_error)));
            return;
        }

        try {
            String selectedCameraId = null;
            String fallbackCameraId = null;

            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    selectedCameraId = id;
                    break;
                }
                if (fallbackCameraId == null) {
                    fallbackCameraId = id;
                }
            }

            if (selectedCameraId == null) {
                selectedCameraId = fallbackCameraId;
            }

            if (selectedCameraId == null) {
                mainHandler.post(() -> listener.onError(context.getString(R.string.face_id_camera_error)));
                return;
            }

            manager.openCamera(selectedCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                    if (isScanning) {
                        mainHandler.post(() -> listener.onError(context.getString(R.string.face_id_camera_error)));
                    }
                }
            }, cameraHandler);

        } catch (CameraAccessException | SecurityException e) {
            Log.e(TAG, "Failed to open camera for Face ID", e);
            mainHandler.post(() -> listener.onError(context.getString(R.string.face_id_camera_error)));
        }
    }

    private void createCameraPreviewSession() {
        if (cameraDevice == null || !textureView.isAvailable()) return;

        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture == null) return;

            texture.setDefaultBufferSize(640, 480);
            Surface surface = new Surface(texture);

            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null || !isScanning) return;
                    captureSession = session;
                    try {
                        captureSession.setRepeatingRequest(builder.build(), null, cameraHandler);
                        mainHandler.post(listener::onCameraReady);
                        scheduleNextAnalysis(200);
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "Failed to start camera repeating preview", e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    if (isScanning) {
                        mainHandler.post(() -> listener.onError(context.getString(R.string.face_id_camera_error)));
                    }
                }
            }, cameraHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "createCameraPreviewSession error", e);
            mainHandler.post(() -> listener.onError(context.getString(R.string.face_id_camera_error)));
        }
    }

    private void scheduleNextAnalysis(long delayMillis) {
        if (!isScanning || cameraHandler == null) return;
        cameraHandler.postDelayed(this::analyzeCurrentFrame, delayMillis);
    }

    private void analyzeCurrentFrame() {
        if (!isScanning) return;

        // Check timeout
        if (SystemClock.elapsedRealtime() - scanStartTime > SCAN_TIMEOUT_MS) {
            stopScanning();
            mainHandler.post(listener::onTimeout);
            return;
        }

        if (isAnalyzing || faceDetector == null) {
            scheduleNextAnalysis(100);
            return;
        }

        isAnalyzing = true;

        mainHandler.post(() -> {
            if (!isScanning || !textureView.isAvailable()) {
                isAnalyzing = false;
                scheduleNextAnalysis(150);
                return;
            }

            Bitmap bitmap = null;
            try {
                // Grab low-resolution bitmap for rapid ML Kit processing (under 3ms)
                bitmap = textureView.getBitmap(320, 320);
            } catch (Exception e) {
                Log.w(TAG, "Error obtaining frame bitmap", e);
            }

            if (bitmap == null) {
                isAnalyzing = false;
                scheduleNextAnalysis(150);
                return;
            }

            final Bitmap finalBitmap = bitmap;
            if (cameraHandler != null) {
                cameraHandler.post(() -> {
                    try {
                        InputImage image = InputImage.fromBitmap(finalBitmap, 0);
                        faceDetector.process(image)
                                .addOnSuccessListener(faces -> handleDetectionResult(faces))
                                .addOnFailureListener(e -> {
                                    isAnalyzing = false;
                                    scheduleNextAnalysis(150);
                                })
                                .addOnCompleteListener(task -> {
                                    finalBitmap.recycle();
                                });
                    } catch (Exception e) {
                        isAnalyzing = false;
                        finalBitmap.recycle();
                        scheduleNextAnalysis(150);
                    }
                });
            } else {
                isAnalyzing = false;
                finalBitmap.recycle();
            }
        });
    }

    private void handleDetectionResult(List<Face> faces) {
        if (!isScanning) return;

        boolean hasFace = false;
        Face selectedFace = null;

        if (faces != null && !faces.isEmpty()) {
            for (Face face : faces) {
                Rect bounds = face.getBoundingBox();
                // Check face has substantial bounding size (avoid noise or far background artifacts)
                if (bounds.width() >= 45 && bounds.height() >= 45) {
                    hasFace = true;
                    selectedFace = face;
                    break;
                }
            }
        }

        if (hasFace) {
            consecutiveDetectedCount++;

            if (isTurnHeadMode) {
                float eulerY = selectedFace.getHeadEulerAngleY(); // Degrees: negative is one side, positive is other

                // Head turn progress increments as user turns
                // In front camera: turning head left or right alters Euler Y
                if (eulerY < -12f) {
                    leftTurnProgress = Math.min(1.0f, leftTurnProgress + 0.35f);
                } else if (eulerY > 12f) {
                    rightTurnProgress = Math.min(1.0f, rightTurnProgress + 0.35f);
                }

                final float curLeft = leftTurnProgress;
                final float curRight = rightTurnProgress;
                final Face currentFace = selectedFace;

                if (curLeft >= 1.0f && curRight >= 1.0f) {
                    // Both left and right turned successfully!
                    isScanning = false;
                    mainHandler.post(() -> {
                        listener.onHeadTurnProgress(1.0f, 1.0f, context.getString(R.string.face_verify_complete_title));
                        listener.onFaceVerified(currentFace);
                        stopScanning();
                    });
                    return;
                }

                // Prompt user based on what's missing
                String prompt;
                if (curLeft < 1.0f && curRight < 1.0f) {
                    prompt = context.getString(R.string.face_verify_instruction_turn_head);
                } else if (curLeft < 1.0f) {
                    prompt = context.getString(R.string.face_verify_instruction_turn_left);
                } else {
                    prompt = context.getString(R.string.face_verify_instruction_turn_right);
                }

                mainHandler.post(() -> {
                    listener.onFaceScanning(true, prompt);
                    listener.onHeadTurnProgress(curLeft, curRight, prompt);
                });

                isAnalyzing = false;
                scheduleNextAnalysis(90);
            } else {
                if (consecutiveDetectedCount >= 2) {
                    // Genuinely detected and confirmed
                    isScanning = false;
                    final Face verifiedFace = selectedFace;
                    mainHandler.post(() -> {
                        listener.onFaceVerified(verifiedFace);
                        stopScanning();
                    });
                    return;
                } else {
                    mainHandler.post(() -> listener.onFaceScanning(true, context.getString(R.string.face_id_sheet_scanning)));
                    isAnalyzing = false;
                    scheduleNextAnalysis(80);
                }
            }
        } else {
            consecutiveDetectedCount = 0;
            mainHandler.post(() -> {
                String prompt = isTurnHeadMode
                        ? context.getString(R.string.face_verify_instruction_turn_head)
                        : context.getString(R.string.face_id_not_detected);
                listener.onFaceScanning(false, prompt);
                listener.onHeadTurnProgress(leftTurnProgress, rightTurnProgress, prompt);
            });
            isAnalyzing = false;
            scheduleNextAnalysis(160);
        }
    }

    /**
     * Stop scanning and release all camera and detector resources.
     */
    public synchronized void stopScanning() {
        isScanning = false;
        isAnalyzing = false;

        try {
            if (captureSession != null) {
                captureSession.close();
                captureSession = null;
            }
        } catch (Exception ignored) {
        }

        try {
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
        } catch (Exception ignored) {
        }

        try {
            if (faceDetector != null) {
                faceDetector.close();
                faceDetector = null;
            }
        } catch (Exception ignored) {
        }

        stopBackgroundThread();
    }
}
