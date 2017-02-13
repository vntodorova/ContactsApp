package com.example.venetatodorova.contacts.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.venetatodorova.contacts.adapters.DrawerAdapter;
import com.example.venetatodorova.contacts.models.DrawerElement;
import com.example.venetatodorova.contacts.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class CameraFragment extends Fragment implements View.OnClickListener,
        DrawerAdapter.FlashListener, DrawerAdapter.ZoomListener, DrawerAdapter.ExposureListener {
    private TextureView textureView;
    private Button button;
    private LinearLayout buttonBar;
    private ActionBarDrawerToggle mDrawerToggle;

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    public static final String PATH = Environment.getExternalStorageDirectory()+"/image.jpg";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    protected CameraDevice cameraDevice;
    protected CameraCaptureSession captureSession;
    protected CaptureRequest.Builder previewRequestBuilder;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private ImageReader reader;
    private Size previewSize;
    private CameraCharacteristics characteristics;
    private String cameraId;
    private CaptureListener listener;
    private boolean flashIsOn = false;
    private Rect zoomCrop;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (CaptureListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        textureView = (TextureView) view.findViewById(R.id.texture);
        textureView.setSurfaceTextureListener(textureListener);
        button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(this);
        view.findViewById(R.id.no_button).setOnClickListener(this);
        view.findViewById(R.id.yes_button).setOnClickListener(this);
        buttonBar = (LinearLayout) view.findViewById(R.id.buttonPanel);

        initDrawerMenu(view);
    }

    private void initDrawerMenu(View view) {
        ArrayList<DrawerElement> list = new ArrayList<>();
        list.add(new DrawerElement(DrawerElement.FLASH_CHECKBOX));
        list.add(new DrawerElement(DrawerElement.ZOOM_SEEKBAR));
        list.add(new DrawerElement(DrawerElement.EXPOSURE_SEEKBAR));

        DrawerLayout mDrawerLayout = (DrawerLayout) view.findViewById(R.id.fragment_camera);
        ListView mDrawerList = (ListView) view.findViewById(R.id.drawer_list);

        mDrawerList.setAdapter(new DrawerAdapter(getActivity(), list, this, this, this));

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int i) {
            camera.close();
            cameraDevice = null;
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    };

    private ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            File file = new File(Environment.getExternalStorageDirectory()+"/image.jpg");
            try (OutputStream out = new FileOutputStream(file)) {
                Image image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                out.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void takePicture() {
        try {
            final CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(reader.getSurface());
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            if (flashIsOn) {
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            } else {
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            }
            captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION,zoomCrop);
            captureSession.stopRepeating();
            captureSession.capture(captureRequestBuilder.build(), null, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface surface = new Surface(texture);
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface, reader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    captureSession = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera(int width, int height) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        try {
            setUpCameraOutputs();
            configureTransform(width, height);
            CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCameraOutputs() {
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {
                previewSize = map.getOutputSizes(ImageFormat.JPEG)[0];
            }
            reader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 10);
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            if (map != null) {
                previewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    public void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == textureView || null == previewSize) {
            return;
        }
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button: {
                takePicture();
                button.setVisibility(View.GONE);
                buttonBar.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.yes_button: {
                listener.onCapture(PATH);
                break;
            }
            case R.id.no_button: {
                buttonBar.setVisibility(View.GONE);
                button.setVisibility(View.VISIBLE);
                createCameraPreview();
            }
            break;
        }
    }

    @Override
    public void onExposurePercentageChange(int percentage) {
        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);

        Range<Integer> range1 = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
        if (range1 != null) {
            int min = range1.getLower();
            int max = range1.getUpper();
            int exposure = ((percentage * (max - min)) / 100) - max;
            Log.v("Exposure", "rate " + exposure);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, exposure);
            updatePreview();
        }
    }

    @Override
    public void onFlashToggle() {
        if(flashIsOn){
            previewRequestBuilder.set(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_OFF);
        } else {
            previewRequestBuilder.set(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_TORCH);
        }
        flashIsOn = !flashIsOn;
        updatePreview();
    }

    @Override
    public void onZoomPercentageChange(int percentage) {
        Rect activePixels = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        Log.v("Zoom","%"+percentage);
        int leftTopX = (percentage*activePixels.width())/200;
        int leftTopY = (percentage*activePixels.height())/200;
        int rightBottomX = activePixels.width() - leftTopX;
        int rightBottomY = activePixels.height() - leftTopY;

        Log.v("Rect","coord:" + leftTopX + " " + leftTopY + " " + rightBottomX + " " + rightBottomY);
        zoomCrop = new Rect(leftTopX,leftTopY,rightBottomX,rightBottomY);
        previewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION,zoomCrop);
        updatePreview();
    }

    public interface CaptureListener {
        void onCapture(String path);
    }
}
