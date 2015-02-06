package com.reality;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;

public class MyGLRenderer implements Renderer 
{	
	private MyGLTriangle  mTriangle = null;
	private Context       mContexto = null; 
	private Mat           camMatrix = null;
	private FloatBuffer           m = null;
	
	// Aux variables
	private float angulo, inc;
	private boolean left;
	
	public MyGLRenderer( Context context ) 
	{
	    this.mTriangle = new MyGLTriangle( );
	    this.mContexto = context;
	    this.angulo    = 0.0f;	    
	    this.inc       = 0.5f;
	    this.left      = true;
	}
	
	
	@Override
	public void onDrawFrame( GL10 gl ) 
	{
	    gl.glClear( GL10.GL_COLOR_BUFFER_BIT | 
	    			GL10.GL_DEPTH_BUFFER_BIT );
	    
	    // Load ModelView
	    gl.glMatrixMode( GL10.GL_MODELVIEW );
	    gl.glLoadIdentity( );
	    
	    if ( m != null )	    	
	    	gl.glLoadMatrixf( m );
	     
	    gl.glRotatef( angulo, 0f, 1f, 0f );
	    gl.glRotatef( 25.0f, 1.0f, 0.0f, 0.0f );
	    
	    gl.glPushMatrix( );
	    gl.glTranslatef( 0.0f, 0.0f, 0.5f );
	    mTriangle.draw( gl ); 
	    gl.glPopMatrix( );

	    gl.glPushMatrix( );
	    gl.glTranslatef( 0.0f, 0.0f, -0.5f );
	    gl.glRotatef( 180.0f, 0.0f, 1.0f, 0.0f );
	    mTriangle.draw( gl );
	    gl.glPopMatrix( );

	    gl.glPushMatrix( );
	    gl.glRotatef( 90.0f, 0.0f, 1.0f, 0.0f );
	    gl.glTranslatef( 0.0f, 0.0f, 0.5f );
	    mTriangle.draw( gl );
	    gl.glPopMatrix( );

	    gl.glPushMatrix( );
	    gl.glRotatef( 270.0f, 0.0f, 1.0f, 0.0f);
	    gl.glTranslatef( 0.0f, 0.0f, 0.5f );
	    mTriangle.draw( gl );
	    gl.glPopMatrix( );
	    
	    gl.glPushMatrix( );
	    gl.glRotatef( 90.0f, 1.0f, 0.0f, 0.0f );
	    gl.glTranslatef( 0.0f, 0.0f, 0.5f );
	    mTriangle.draw( gl );
	    gl.glPopMatrix( );

	    gl.glPushMatrix( );
	    gl.glRotatef( 270.0f, 1.0f, 0.0f, 0.0f );
	    gl.glTranslatef( 0.0f, 0.0f, 0.5f );
	    mTriangle.draw( gl );
	    gl.glPopMatrix( );

	    if ( !left )
	    	angulo += inc;
	    else
	    	angulo -= inc;
	    
	} // onDrawFrame
	
	
	@Override
	public void onSurfaceChanged( GL10 gl, int width, int height ) 
	{		
	    if( height == 0 )		                //Previene una divisi�n por cero
	        height = 1;                         
	    	   
	    gl.glMatrixMode( GL10.GL_PROJECTION );
	    gl.glLoadIdentity( );
	    
	    int size = (int) camMatrix.total( ) * camMatrix.channels( );
	    float[] buffer = new float[size];
	    
	    camMatrix.get( 0, 0, buffer );	    
	    
	    float fx = buffer[0];
	    float fy = buffer[4];
		    
	    float fovy = (float) ( ( 2 * ( Math.atan( ( 0.5f*height ) / fy ) ) * 180.0f ) / Math.PI );
	    float aspect = ( width * fy ) / ( height * fx );
	    
	    float near = 0.1f;
	    float far = 100.0f;
	    
	    GLU.gluPerspective( gl, fovy, aspect, near, far );
	    gl.glViewport( 0, 0, width, height );
	    
	    gl.glMatrixMode( GL10.GL_MODELVIEW );
	    gl.glLoadIdentity( );
	    
	} // onSurfaceChanged
	
	
	@Override
	public void onSurfaceCreated( GL10 gl, EGLConfig config ) 
	{
	    // Cargamos la textura para el cuadrado
	    // cuadrado.loadGLTexture(gl, this.contexto);
	    // Activamos el mapeo de texturas
	    gl.glEnable( GL10.GL_TEXTURE_2D );
	    
	    // Activamos el sombreado suave
	    gl.glShadeModel( GL10.GL_SMOOTH );
	    
	    // Esblecemos el fondo negro
	    gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.5f );
	    
	    // Limpiamos el buffer de profundidad
	    gl.glClearDepthf( 1.0f );
	    
	    // Activamos test de profundidad
	    gl.glEnable( GL10.GL_DEPTH_TEST );
	    
	    // Tipo de test a realizar
	    gl.glDepthFunc( GL10.GL_LEQUAL );
	    
	    // Activamos la prespectiva adecuada
	    gl.glHint( GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST );
	    
	} // onSurfaceCreated
	
	
	public void updateRt( Mat rvecs, Mat tvecs )
	{		
		Mat R = new Mat( 3, 3, CvType.CV_32F );
		float[] M = new float[16];		
		float[] t = new float[3];
		
		rvecs.convertTo( rvecs, CvType.CV_32F );
		tvecs.convertTo( tvecs, CvType.CV_32F );		
		
		tvecs.get( 0, 0, t );	
		
		Calib3d.Rodrigues( rvecs , R );		
		R.t().get( 0 , 0, M );		
		
		M[12] = t[0];
		M[13] = -t[1];
		M[14] = -t[2];	
		M[15] = 1;
		
		ByteBuffer mByteBuffer = ByteBuffer.allocateDirect( 16 * Float.SIZE );
		mByteBuffer.order( ByteOrder.nativeOrder( ) );		
		this.m = mByteBuffer.asFloatBuffer( );
		this.m.put( M );		
		this.m.position( 0 );
	}
	
	public void setCamMatrix( Mat camMatrix ) { this.camMatrix = camMatrix; }
	
	public void setAngle( boolean left ) { this.left = left; }
	
	public void increaseAngle( ) { inc += 1; }
	
	public void decreaseAngle( ) { inc -= 1; }
	
	public void setAngle( float angle ) { angulo = angle; }
}