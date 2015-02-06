package com.reality;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Size;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class ARMainActivity extends Activity implements CvCameraViewListener2, OnTouchListener
{	
	private static final String TAG = "com.reality/AR_MainActivity";
	
	// Camera parameters (intrinsec parameters and distortion coefficients)
	private Mat    camMatrix = null;
	private MatOfDouble dist = null;
	
	// OpenCV variables
	private CameraBridgeViewBase mOpenCvCameraView;
	private boolean mIsJavaCamera = true;
	private Size     patternSize  = null;
	private MatOfPoint3f    objp  = null;
	private MatOfPoint2f   imgpts = null;
	private Mat             tvecs = null;
	private Mat             rvecs = null;
	
	// OpenGL variables
	private MyGLRenderer mRenderer = null;
	private GLSurfaceView  mGLView = null;	

	// Aux variables
	private boolean       mJavaCamera = true;
	private boolean             ready = false;
	private String           toastMsg = null;
	private MenuItem        mItemInit = null;
	private FrameLayout        mFrame = null;
	
	// 3D object to be drawn
	private MatOfPoint3f cube = null;
	private float      height = 3.0f;
		
	// Linking library
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback( this ) 
	{
        @Override
        public void onManagerConnected( int status ) {
            switch ( status ) {
                case LoaderCallbackInterface.SUCCESS:
                {
                	System.loadLibrary( "ProcessFrame" );
                	
                	mOpenCvCameraView.enableView( );
                } break;
                default:
                {
                    super.onManagerConnected( status );
                } break;
            }
        }
    };

	@Override
	protected void onCreate( Bundle savedInstanceState ) 
	{
		super.onCreate( savedInstanceState );
		
		getWindow( ).addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		
		mFrame = new FrameLayout( this );
		setContentView( R.layout.surface_view );
		
		// Init Camera View
		mOpenCvCameraView = ( CameraBridgeViewBase )findViewById( R.id.java_surface_view );        
		mOpenCvCameraView.setMaxFrameSize( 640, 480 );
        mOpenCvCameraView.setVisibility( SurfaceView.VISIBLE );
        mOpenCvCameraView.setCvCameraViewListener( this );
        
        // Remove child from layout
        (( ViewGroup )mOpenCvCameraView.getParent( )).removeView( mOpenCvCameraView );
        
        // Add the camera view
        mFrame.addView( mOpenCvCameraView );
        
        setContentView( mFrame );
   
	} // onCreate
	
	
	@Override
	public void onPause( )
	{
		super.onPause( );
		if ( mOpenCvCameraView != null )
			 mOpenCvCameraView.disableView( );
	} // onPause
	
	
	@Override
	public void onResume( )
	{
		super.onResume( );		
        OpenCVLoader.initAsync( OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback );
	} // onResume
	
	
	@Override
	public void onDestroy( )
	{
		super.onDestroy( );
		if ( mOpenCvCameraView != null )
			 mOpenCvCameraView.disableView( );
	} // onDestroy
	
	
	@Override
    public boolean onCreateOptionsMenu( Menu menu ) 
	{
        mItemInit = menu.add( "Initialize and Start!" );
        return true;
    } // onCreateOptionsMenu
	

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    	// Select this option to initialize everything
        if ( item == mItemInit ) 
        { 
        	patternSize = new Size( 12, 9 );
        	
        	objp    = new MatOfPoint3f( _initPoint3Vector( ) );
        	imgpts  = new MatOfPoint2f( );
        	
        	tvecs = new Mat( );
        	rvecs = new Mat( );
        	
        	camMatrix = new Mat ( 3, 3, CvType.CV_32F );
        	camMatrix.put( 0, 0, 522.62901766f );
        	camMatrix.put( 0, 1,          0.0f );
        	camMatrix.put( 0, 2, 327.92039763f );
        	camMatrix.put( 1, 0,          0.0f );
        	camMatrix.put( 1, 1, 700.89362064f );
        	camMatrix.put( 1, 2, 240.63088507f );
        	camMatrix.put( 2, 0,          0.0f );
        	camMatrix.put( 2, 1,          0.0f );
        	camMatrix.put( 2, 2,          1.0f );
        	
        	dist = new MatOfDouble( );
        	dist.put( 0 , 0,  0.33178179f );
        	dist.put( 0 , 1, -1.47710513f );
        	dist.put( 0 , 2, -0.00376001f );
        	dist.put( 0 , 3,  0.01111760f );
        	dist.put( 0 , 4,  2.04812404f );
        	
        	cube = new MatOfPoint3f( _initCoordCube( height ) );
        	
        	// Init OpenGL View
        	mRenderer = new MyGLRenderer( this );
        	mRenderer.setCamMatrix( camMatrix );
            mGLView = new GLSurfaceView( this );
            mGLView.setZOrderMediaOverlay( true );
            mGLView.setEGLConfigChooser( 8,8,8,8,16,0 );
            mGLView.setRenderer( mRenderer );
            mGLView.getHolder( ).setFormat( PixelFormat.TRANSPARENT );
            mGLView.setOnTouchListener( this );
            mFrame.addView( mGLView );
            
            setContentView( mFrame );
        	
        	ready = true;
        }       

        return true;
    } // onOptionsItemSelected
    
    
    @Override
	public boolean onTouch( View v, MotionEvent event ) 
    {
    	if ( event.getAction( ) == MotionEvent.ACTION_DOWN )			
			if ( event.getX( ) <= 550 )
				mRenderer.decreaseAngle( );
			else 
				mRenderer.increaseAngle( );
		
		return true;
	}	
    
	
	@Override
	public void onCameraViewStarted( int width, int height ) {}
	
	@Override
	public void onCameraViewStopped( ) {}
	
	
	@Override
	public Mat onCameraFrame( CvCameraViewFrame inputFrame ) 
	{
		Mat rgba = inputFrame.rgba( );
		
		if ( ready )
		{			
			if ( nativeProjectPoints( rgba.getNativeObjAddr( )
					, objp.getNativeObjAddr( )
					, camMatrix.getNativeObjAddr( )
					, dist.getNativeObjAddr( )
					, rvecs.getNativeObjAddr( )
					, tvecs.getNativeObjAddr( ) ) )
			{			
				mRenderer.updateRt( rvecs, tvecs );
			}
		}
		
		return rgba;
	} // onCameraFrame
	
	
	private Point3[] _initPoint3Vector( )
	{
		Point3[] vaux = new Point3[ ( int ) ( patternSize.height * patternSize.width ) ];
		
		int index = 0;
		for ( int i = 0; i < patternSize.height; i++ )
		{
			for ( int j = 0; j < patternSize.width; j++ )
			{
				vaux[index++] = new Point3( j, i, 0 );
			}
		}
		
		return vaux;
	} // _initPoint3Vector

	private Point3[] _initCoordCube( float height )
	{
		Point3[] vaux = new Point3[ 8 ];
		
		vaux[0] = new Point3( 4.0f, 3.0f, 0.0f );
		vaux[1] = new Point3( 4.0f, 6.0f, 0.0f );
		vaux[2] = new Point3( 7.0f, 6.0f, 0.0f );
		vaux[3] = new Point3( 7.0f, 3.0f, 0.0f );
		vaux[4] = new Point3( 4.0f, 3.0f, -height );
		vaux[5] = new Point3( 4.0f, 6.0f, -height );
		vaux[6] = new Point3( 7.0f, 6.0f, -height );
		vaux[7] = new Point3( 7.0f, 3.0f, -height );
		
		return vaux;
	} // _initCoordCube	

	private static native boolean nativeProjectPoints( long rgba
											, long objp
											, long mtx
											, long dist
											, long rvecs
											, long tvecs );
	
}
