/******   eliptic.h   *****/
/****************************************************************
*                                                               *
*       These are structures used to create elliptic curve      *
*  points and parameters.  "form" is a just a fast way to check *
*  if a2 == 0.                                                  *
*               form            equation                        *
*                                                               *
*                0              y^2 + xy = x^3 + a_6            *
*                1              y^2 + xy = x^3 + a_2*x^2 + a_6  *
*                                                               *
****************************************************************/

typedef struct 
{
        INDEX   form;
        FIELD2N  a2;
        FIELD2N  a6;
} CURVE;

/*  coordinates for a point  */

typedef struct 
{
        FIELD2N  x;
        FIELD2N  y;
} POINT1;





 
