/******   eliptic.c   *****/
/************************************************************************
*                                                                       *
*       Elliptic curves over Galois Fields.  These routines find POINT1s *
*  on a curve, add and double POINT1s for type 1 optimal normal bases.   *
*                                                                       *
*	For succint explanaition, see Menezes, page 92          	        *
*																		*
*		This file modified 6/23/97 to work with generalized ONB and 	*
*	tunable field sizes.  mgr											*
*                                                                       *
************************************************************************/

#include <stdio.h>
extern "C"
{
#include "field2n.h"
#include "eliptic.h"
}
extern "C"
{

void opt_inv(FIELD2N *a,FIELD2N *result); 
void rot_left(FIELD2N *a);
void rot_right(FIELD2N *a);
extern int null(FIELD2N *a);
 void opt_mul(FIELD2N *a,FIELD2N *b,FIELD2N *c);
void copy(FIELD2N *a,FIELD2N *b);
	void genlambda();
 void genlambda2();

int opt_quadratic(FIELD2N *a,FIELD2N *b,FIELD2N *y);
void fofx(FIELD2N *x, CURVE *curv,FIELD2N *f);
void esum ();
void edbl ();
void esub ();
void copy_POINT1 ();
void  elptic_mul(FIELD2N	*k,POINT1 *p,POINT1 *r,CURVE *curv);
void one(FIELD2N*);
extern INDEX	Lambda[2][field_prime];
extern INDEX	lg2_m;
}
/************************************************************************
*  Note that the following is obvious to mathematicians.  I thought it  *
*  was pretty cool when I discovered it myself, <sigh>.			*
*                                                                       *
*       Routine to solve quadradic equation.  Enter with coeficients    *
*  a and b and it returns solutions y[2]: y^2 + ay + b = 0.             *
*  If Tr(b/a^2) != 0, returns y=0 and error code 1.                     *
*  If Tr(b/a^2) == 0, returns y[2] and error code 0.                    *
*  If solution fails, returns y=0 and error code 2.                     *
*                                                                       *
*      Algorithm used based on normal basis GF math.  Since (a+b)^2 =   *
*  a^2 + b^2 it follows that (a+b)^.5 = a^.5 + b^.5.  Note that squaring*
*  is a left shift and rooting is a right shift in a normal basis.      *
*  Transforming the source equation with y = ax and dividing by a^2     *
*  gives:                                                               *
*               x^2 + x + b/a^2 = 0                                     *
*                                                                       *
*       or      x = x^.5 + (b/a^2)^.5                                   *
*                                                                       *
*  Let k_i = the ith significant bit of (b/a^2)^.5 and                  *
*      x_i = the ith significant bit of x.                              *
*  The above equation is equivelent to a bitwise representation as      *
*                                                                       *
*               x_i = x_(i+1) + k_i                                     *
*       or                                                              *
*               x(i+1) = x_i + k_i.                                     *
*                                                                       *
*  Since both x and x+1 are solutions, and 1 is represented by all      *
*  bits set in a normal basis, we can start with x_0 = 0 or 1 at our    *
*  pleasure and use the recursion relation to discover every bit of x.  *
*  The answer is then ax and ax+a returned in y[0] and y[1] respectively*
*  If the sum of x_(n-1) + k_(n-1) != x_0, returns error code 2 and     *
*  y = 0.                                                               *
*                                                                       *
*       error code                      returns                         *
*          0                    y[0] and y[1] values                    *
*          1                    y[0] = y[1] = 0                         *
*          2                    mathematicly impossible !!!!            *
*                                                                       *
************************************************************************/

int opt_quadratic(FIELD2N *a,FIELD2N *b,FIELD2N *y)

{
        INDEX   i, el, bits;
        FIELD2N  z, k, a2;
        ELEMENT  r, t, mask;

/*  test for a=0. Return y = square root of b.  */

        r = 0;
        SUMLOOP(i) r |= a->e[i];
        if (!r) 
        {
			copy( b, &y[0]);
			rot_right( &y[0]);
			copy( &y[0], &y[1]);
           return(0);
        }

/*  find a^-2  */

        opt_inv( a, &a2);
        rot_left(&a2);

/*  find k=(b/a^2)^.5 */

        opt_mul( b, &a2, &k);
        rot_right(&k);
        r = 0;

/*  check that Tr(k) is zero.  Combine all words first. */

        SUMLOOP(i)  r ^= k.e[i];

/*  take trace of word, combining half of all the bits each time */

        mask = -1L;
        for (bits = WORDSIZE/2; bits > 0; bits >>= 1)
        {
        	mask >>= bits;
            r = ((r & mask) ^ (r >> bits));
        } 

/*  if not zero, return error code 1.  */

        if (r) 
        {
           null(&y[0]);
           null(&y[1]);
           return(1);
        }

/*  POINT1 is valid, proceed with solution.  mask POINT1s to bit i,
which is known, in x bits previously found and k (=b/a^2)^.5.  */

        null(&z);
        mask = 1;
        for (bits=0; bits < NUMBITS ; bits++) 
        {

/* source long word could be different than destination  */

           i = NUMWORD - bits/WORDSIZE;
           el = NUMWORD - (bits + 1)/WORDSIZE;

/*  use present bits to compute next one */

           r = k.e[i] & mask;
           t = z.e[i] & mask;
           r ^= t;

/*  same word, so just shift result up */

           if ( el == i ) 
           {
              r <<= 1;
              z.e[el] |= r;
              mask <<= 1;
           } 
           else 
           {

/*  different word, reset mask and use a 1 */

              mask = 1;
              if (r) z.e[el] = 1;
           }
        }

/*  test that last bit generates a zero */

        r = k.e[0] & UPRBIT;
        t = z.e[0] & UPRBIT;
        if ( r^t ) 
        {
           null(&y[0]);
           null(&y[1]);
           return(2);
        }

/*  convert solution back via y = az */

        opt_mul(a, &z, &y[0]);

/*  and create complementary (z+1) solution y = az + a */

		null (&y[1]);
        SUMLOOP(i) y[1].e[i] = y[0].e[i] ^ a->e[i];

/*  no errors, bye!  */

        return(0);
}

/*  compute R.H.S. f(x) = x^3 + a2*x^2 + a6  
    curv.form = 0 implies a2 = 0, so no extra multiply.  
    curv.form = 1 is the "twist" curve.
*/

void fofx(FIELD2N *x, CURVE *curv,FIELD2N *f)

{

        FIELD2N x2,x3;
        INDEX i;

        copy(x, &x2);
        rot_left(&x2);
        opt_mul(x, &x2, &x3);
        if (curv->form) opt_mul(&x2, &curv->a2, f);
		else null(f);
        SUMLOOP(i)
             f->e[i] ^= (x3.e[i] ^ curv->a6.e[i]);
}

/****************************************************************************
*                                                                           *
*   Implement elliptic curve POINT1 addition for optimal normal basis form.  *
*  This follows R. Schroeppel, H. Orman, S. O'Mally, "Fast Key Exchange with*
*  Elliptic Curve Systems", CRYPTO '95, TR-95-03, Univ. of Arizona, Comp.   *
*  Science Dept.                                                            *
*                                                                           *
*   This version is faster for inversion processes requiring fewer          *
*  multiplies than projective math version.  For NUMBITS = 148 or 226 this  *
*  is the case because only 10 multiplies are required for inversion but    *
*  15 multiplies for projective math.  I leave it as a paper to be written  *
*  [HINT!!] to propagate TR-95-03 to normal basis inversion.  In that case  *
*  inversion will require order 2 multiplies and this method would be far   *
*  superior to projective coordinates.                                      *
****************************************************************************/

void esum (POINT1   *p1,POINT1 *p2,POINT1 *p3,CURVE *curv)
{
    INDEX   i;
    FIELD2N  x1, y1, theta, onex, theta2;

/*  compute theta = (y_1 + y_2)/(x_1 + x_2)  */

    null(&x1);
    null(&y1);
    SUMLOOP(i) 
    {
		x1.e[i] = p1->x.e[i] ^ p2->x.e[i];
		y1.e[i] = p1->y.e[i] ^ p2->y.e[i];
    }
    opt_inv( &x1, &onex);
    opt_mul( &onex, &y1, &theta);
    copy( &theta, &theta2);
    rot_left(&theta2);

/*  with theta and theta^2, compute x_3  */

    if (curv->form)
		SUMLOOP (i)
	    	p3->x.e[i] = theta.e[i] ^ theta2.e[i] ^ p1->x.e[i] ^ p2->x.e[i]
			 ^ curv->a2.e[i];
    else
		SUMLOOP (i)
	    	p3->x.e[i] = theta.e[i] ^ theta2.e[i] ^ p1->x.e[i] ^ p2->x.e[i];

/*  next find y_3  */

    SUMLOOP (i) x1.e[i] = p1->x.e[i] ^ p3->x.e[i];
    opt_mul( &x1, &theta, &theta2);
    SUMLOOP (i) p3->y.e[i] = theta2.e[i] ^ p3->x.e[i] ^ p1->y.e[i];
}

/*  elliptic curve doubling routine for Schroeppel's algorithm over normal
    basis.  Enter with p1, p3 as source and destination as well as curv
    to operate on.  Returns p3 = 2*p1.
*/

void edbl (POINT1 *p1,POINT1 *p3,CURVE *curv)
{
    FIELD2N  x1, y1, theta, theta2, t1;
    INDEX   i;

/*  first compute theta = x + y/x  */

    opt_inv( &p1->x, &x1);
    opt_mul( &x1, &p1->y, &y1);
    SUMLOOP (i) theta.e[i] = p1->x.e[i] ^ y1.e[i];

/*  next compute x_3  */

    copy( &theta, &theta2);
    rot_left(&theta2);
    if(curv->form)
		SUMLOOP (i) p3->x.e[i] = theta.e[i] ^ theta2.e[i] ^ curv->a2.e[i];
    else
		SUMLOOP (i) p3->x.e[i] = theta.e[i] ^ theta2.e[i];

/*  and lastly y_3  */

    one( &y1);
    SUMLOOP (i) y1.e[i] ^= theta.e[i];
    opt_mul( &y1, &p3->x, &t1);
    copy( &p1->x, &x1);
    rot_left( &x1);
    SUMLOOP (i) p3->y.e[i] = x1.e[i] ^ t1.e[i];
}

/*  subtract two POINT1s on a curve.  just negates p2 and does a sum.
    Returns p3 = p1 - p2 over curv.
*/

void esub (POINT1   *p1,POINT1 *p2,POINT1 *p3,CURVE *curv)
{
    POINT1   negp;
    INDEX   i;

    copy ( &p2->x, &negp.x);
    null (&negp.y);
    SUMLOOP(i) negp.y.e[i] = p2->x.e[i] ^ p2->y.e[i];
    esum (p1, &negp, p3, curv);
}

/*  need to move POINT1s around, not just values.  Optimize later.  */

void copy_POINT1 (POINT1 *p1,POINT1 *p2)

{
	copy (&p1->x, &p2->x);
	copy (&p1->y, &p2->y);
}

/*  Routine to compute kP where k is an integer (base 2, not normal basis)
	and P is a POINT1 on an elliptic curve.  This routine assumes that K
	is representable in the same bit field as x, y or z values of P.
	This is for simplicity, larger or smaller fields can be independently 
	implemented.
    Enter with: integer k, source POINT1 P, curve to compute over (curv) and 
    Returns with: result POINT1 R.

  Reference: Koblitz, "CM-Curves with good Cryptografic Properties", 
	Springer-Verlag LNCS #576, p279 (pg 284 really), 1992
*/

void  elptic_mul(FIELD2N	*k,POINT1 *p,POINT1 *r,CURVE *curv)

{
	char		blncd[NUMBITS+1];
	INDEX		bit_count, i;
	ELEMENT		notzero;
	FIELD2N		number;
	POINT1		temp;

/*  make sure input multiplier k is not zero.
	Return POINT1 at infinity if it is.
*/
	copy( k, &number);
	notzero = 0;
	SUMLOOP (i) notzero |= number.e[i];
	if (!notzero)
	{
		null (&r->x);
		null (&r->y);
		return;
	}

/*  convert integer k (number) to balanced representation.
	Called non-adjacent form in "An Improved Algorithm for
	Arithmetic on a Family of Elliptic Curves", J. Solinas
	CRYPTO '97. This follows algorithm 2 in that paper.
*/
	bit_count = 0;
	while (notzero)
	{
	/*  if number odd, create 1 or -1 from last 2 bits  */
	
		if ( number.e[NUMWORD] & 1 )
		{
			blncd[bit_count] = 2 - (number.e[NUMWORD] & 3);
			
	/*  if -1, then add 1 and propagate carry if needed  */
			
			if ( blncd[bit_count] < 0 )
			{
				for (i=NUMWORD; i>=0; i--)
				{
					number.e[i]++;
					if (number.e[i]) break;
				}
			}
		}
		else
			blncd[bit_count] = 0;
	
	/*  divide number by 2, increment bit counter, and see if done  */
	
		number.e[NUMWORD] &= ~0 << 1;
		rot_right( &number);
		bit_count++;
		notzero = 0;
		SUMLOOP (i) notzero |= number.e[i];
	}
		
/*  now follow balanced representation and compute kP  */

	bit_count--;
	copy_POINT1(p,r);		/* first bit always set */
	while (bit_count > 0) 
	{
	  edbl(r, &temp, curv);
	  bit_count--;
	  switch (blncd[bit_count]) 
	  {
	     case 1: esum (p, &temp, r, curv);
				 break;
	     case -1: esub (&temp, p, r, curv);
				  break;
	     case 0: copy_POINT1 (&temp, r);
	   }
	}
}

/*  One is not what it appears to be.  In any normal basis, "1" is the sum of
all powers of the generator.  So this routine puts ones to fill the number size
being used in the address of the FIELD2N supplied.  */

void one (FIELD2N *place)

{
	INDEX i;

	SUMLOOP(i) place->e[i] = -1L;
	place->e[0] &= UPRMASK;
}

void opt_mul(FIELD2N *a,FIELD2N *b,FIELD2N *c)

{
	INDEX i, j;
	INDEX 	k, zero_index, one_index;
	ELEMENT bit, temp;
	FIELD2N	amatrix[NUMBITS], copyb;
	
/*  clear result and copy b to protect original  */

	null(c);
	copy(b, &copyb);

/*  To perform the multiply we need two rotations of the input a.  Performing all
	the rotations once and then using the Lambda vector as an index into a table
	makes the multiply almost twice as fast.
*/

	copy( a, &amatrix[0]);
	for (i = 1; i < NUMBITS; i++)
	{
		copy( &amatrix[i-1], &amatrix[i]);
		rot_right( &amatrix[i]);
	}

/*  Lambda[1][0] is non existant, deal with Lambda[0][0] as speical case.  */

	zero_index = Lambda[0][0];
	SUMLOOP (i) c->e[i] = copyb.e[i] & amatrix[zero_index].e[i];

/*  main loop has two lookups for every position.  */

	for (j = 1; j<NUMBITS; j++)
	{
		rot_right( &copyb);
		zero_index = Lambda[0][j];
		one_index = Lambda[1][j];
		SUMLOOP (i) c->e[i] ^= copyb.e[i] &
					(amatrix[zero_index].e[i] ^ amatrix[one_index].e[i]);
	}
}

void rot_left(FIELD2N *a)

{
        INDEX i;
        ELEMENT bit,temp;

        bit = (a->e[0] & UPRBIT) ? 1L : 0L;
        for (i=NUMWORD; i>=0; i--) {
           temp = (a->e[i] & MSB) ? 1L : 0L;
           a->e[i] = ( a->e[i] << 1) | bit;
           bit = temp;
        }
        a->e[0] &= UPRMASK;
}

void opt_inv(FIELD2N *a,FIELD2N *result)

{
	FIELD2N	shift, temp;
	INDEX	m, s, r, rsft;
	
/*  initialize s to lg2_m computed in genlambda.  Since msb is always set,
	initialize result to input a and skip first math loop.
*/

	s = lg2_m - 1;
	copy( a, result);
	m = NUMBITS - 1;

/*  create window over m and walk up chain of terms  */

	while (s >= 0)
	{
		r = m >> s;
		copy( result, &shift);
		for (rsft = 0; rsft < (r>>1); rsft++) rot_left( &shift);
		opt_mul( result, &shift, &temp);
		if ( r&1 )			/* if window value odd  */
		{
			rot_left( &temp);	/*  do extra square  */
			opt_mul( &temp, a, result);	/*  and multiply  */
		}
		else copy( &temp, result);
		s--;
	}
	rot_left(result);		/* final squaring  */
}

void rot_right(FIELD2N *a)

{
        INDEX i;
        ELEMENT bit,temp;

        bit = (a->e[NUMWORD] & 1) ? UPRBIT : 0L;
        SUMLOOP(i) {
           temp = ( a->e[i] >> 1)  | bit;
           bit = (a->e[i] & 1) ? MSB : 0L;
           a->e[i] = temp;
        }
        a->e[0] &= UPRMASK;
}
