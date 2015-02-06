/*
 * ProcessFrame.cpp
 *
 *  Created on: 29/12/2014
 *      Author: Daniel Mancebo
 */
#include <jni.h>

#include <opencv2/core/core.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/imgproc/imgproc.hpp>

using namespace std;
using namespace cv;

extern "C" {

JNIEXPORT jboolean JNICALL Java_com_reality_ARMainActivity_nativeProjectPoints( JNIEnv *env
														, jclass
														, jlong rgba
														, jlong objp
														, jlong mtx
														, jlong dist
														, jlong rvecs
														, jlong tvecs )
{
	Mat mgray;
	vector<Point2f> corners;
	Size patternSize( 12, 9 );

	cvtColor( *(Mat*)rgba, mgray, CV_RGBA2GRAY );
	if ( findChessboardCorners( mgray, patternSize, corners, CALIB_CB_FAST_CHECK ) )
	{
		/*cornerSubPix( *gray, corners, Size( 11,11 ), Size( -1,-1 )
				, TermCriteria(CV_TERMCRIT_EPS + CV_TERMCRIT_ITER, 30, 0.1) );*/

		solvePnPRansac( *(Mat*)objp, corners, *(Mat*)mtx, *(Mat*)dist, *(Mat*)rvecs, *(Mat*)tvecs );

		//projectPoints( *(Mat*)coord, rvecs, tvecs, *(Mat*)mtx, *(Mat*)dist, *(Mat*)imgpts );
		//drawChessboardCorners( *(Mat*)rgba, patternSize, Mat(corners), true );

		return true;
	}

	return false;
} // nativeOnCameraFrame

JNIEXPORT void JNICALL Java_com_reality_ARMainActivity_nativeDrawLineCoord( JNIEnv *env
														, jclass
														, jlong rgba
														, jdouble x1, jdouble y1
														, jdouble x2, jdouble y2 )
{
	line( *(Mat*)rgba, Point2f( x1,y1 ), Point2f( x2,y2 ), Scalar(255,255,0), 3 );
}

}
