/*** field2n.h ***/

#define WORDSIZE	(sizeof(int)*8)
#define NUMBITS		191
#define TYPE2
/*#undef TYPE2 */

#ifdef TYPE2
#define field_prime	((NUMBITS<<1)+1)
#else
#define field_prime (NUMBITS+1)
#endif

#define	NUMWORD		(NUMBITS/WORDSIZE)
#define UPRSHIFT	(NUMBITS%WORDSIZE)
#define MAXLONG1		(NUMWORD+1)

#define MAXBITS		(MAXLONG1*WORDSIZE)
#define MAXSHIFT	(WORDSIZE-1)
#define MSB			(1L<<MAXSHIFT)

#define UPRBIT		(1L<<(UPRSHIFT-1))
#define UPRMASK		(~(-1L<<UPRSHIFT))
#define SUMLOOP(i)	for(i=0; i<MAXLONG1; i++)

typedef	short int INDEX;

typedef unsigned long ELEMENT;

typedef struct {
	ELEMENT 	e[MAXLONG1];
}  FIELD2N;