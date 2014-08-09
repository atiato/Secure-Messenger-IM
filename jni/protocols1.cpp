/********************************************************************************
*																				*
*		Routines to implement protocols, Diffie-Hellman, Massey-Omura and 		*
*	ElGamal for elliptic curve analogs.  Data transfer routines not included.	*
*																				*
********************************************************************************/
//#pragma comment(lib, "wininet.lib")  
#include <stdio.h>
//#include <windows.h>
//#include <wininet.h>  
//#include "process.h"
//#include "Win32InputBox.h"
#include <ios>
#include <sstream>
#include <iostream>
#include <string>
#include <iomanip>
#include <cstdlib>
#include "time.h"
#include <algorithm>
#include <sstream>
#include <iostream>
#include <iterator>
#include <stdexcept>
#include <jni.h>
#include <android/log.h>

using namespace std;

std::string Encrypt(std::string data);
std::string DeCrypt(std::string resulted);
std::string ConvertJString(JNIEnv* env, jstring str);
extern "C"
{
#include "field2n.h"
#include "eliptic.h"
typedef unsigned long word32;
typedef long keysched[32];
 
typedef struct {
        unsigned char b[8];
} chunk;
}

std::string process_select_list();
std::string tohex(std::string const& data);

extern "C"
{
INDEX log_2(ELEMENT x);

extern int opt_inv(); 
extern int rot_left(); 
extern int rot_right(); 
extern void null(FIELD2N *a);
extern int opt_mul(); 
extern void copy(FIELD2N *a,FIELD2N *b);
 void	genlambda();
 void genlambda2();
extern int fsetkey(char key[8], keysched *ks);
extern int fencrypt(char block[8], int decrypt, keysched *ks);
extern int opt_quadratic(FIELD2N *a,FIELD2N *b,FIELD2N *y);
void fofx(FIELD2N *x, CURVE *curv,FIELD2N *f);
void esum ();
void edbl ();
void esub ();
void copy_POINT1 ();
extern void elptic_mul(FIELD2N	*k,POINT1 *p,POINT1 *r,CURVE *curv);
void one(FIELD2N*);
void random_field();
void Mother();
void opt_embed( FIELD2N	*data, CURVE	*curv, INDEX	incrmt, INDEX root, POINT1	*pnt);
void DH_gen_send_key();
void DH_key_share();
extern void exit(int i);
//void send_elgamal();
//void receive_elgamal();
//void ECKGP();
void rand_curve ( );
void rand_POINT1();
void print_field();
void print_POINT1();
void print_curve();
//void authen_secret();

INDEX	lambdaa[2][field_prime];
INDEX	lg2_mm;

}
std::wstring s2ws(const std::string& s);
//void test_inputbox(bool bMultiLine = false);
//LRESULT CALLBACK WindowProcedure (HWND, UINT, WPARAM, LPARAM);  
/*  random seed is accessable to everyone, not best way, but functional.  */

unsigned long random_seed;
bool bDone = false; 
/*  below is from Mother code, till end of mother.  Above is all my fault.  */

#include <string.h>

static short mother1[10];
static short mother2[10];
static short mStart=1;

#define m16Long 65536L                          /* 2^16 */
#define m16Mask 0xFFFF          /* mask for lower 16 bits */
#define m15Mask 0x7FFF                  /* mask for lower 15 bits */
#define m31Mask 0x7FFFFFFF     /* mask for 31 bits */
#define m32Double  4294967295.0  /* 2^32-1 */

/* Mother **************************************************************
|       George Marsaglia's The mother of all random number generators
|               producing uniformly distributed pseudo random 32 bit values with
|               period about 2^250.
|
|       The arrays mother1 and mother2 store carry values in their
|               first element, and random 16 bit numbers in elements 1 to 8.
|               These random numbers are moved to elements 2 to 9 and a new
|               carry and number are generated and placed in elements 0 and 1.
|       The arrays mother1 and mother2 are filled with random 16 bit values
|               on first call of Mother by another generator.  mStart is the switch.
|
|       Returns:
|       A 32 bit random number is obtained by combining the output of the
|               two generators and returned in *pSeed.  It is also scaled by
|               2^32-1 and returned as a double between 0 and 1
|
|       SEED:
|       The inital value of *pSeed may be any long value
|
|       Bob Wheeler 8/8/94
|
|	removed double return since I don't need it.  mgr
*/


void Mother(unsigned long *pSeed)
{
        unsigned long  number,
                       number1,
                       number2;
        short          n,
                       *p;
        unsigned short sNumber;

                /* Initialize motheri with 9 random values the first time */
        if (mStart) {
                sNumber= *pSeed&m16Mask;   /* The low 16 bits */
                number= *pSeed&m31Mask;   /* Only want 31 bits */

                p=mother1;
                for (n=18;n--;) {
                        number=30903*sNumber+(number>>16);   
				/* One line multiply-with-cary */
                        *p++=sNumber=number&m16Mask;
                        if (n==9)
                                p=mother2;
                }
                /* make cary 15 bits */
                mother1[0]&=m15Mask;
                mother2[0]&=m15Mask;
                mStart=0;
        }

                /* Move elements 1 to 8 to 2 to 9 */
        memmove(mother1+2,mother1+1,8*sizeof(short));
        memmove(mother2+2,mother2+1,8*sizeof(short));

                /* Put the carry values in numberi */
        number1=mother1[0];
        number2=mother2[0];

                /* Form the linear combinations */

number1+=1941*mother1[2]+1860*mother1[3]+1812*mother1[4]+1776*mother1[5]+
         1492*mother1[6]+1215*mother1[7]+1066*mother1[8]+12013*mother1[9];

number2+=1111*mother2[2]+2222*mother2[3]+3333*mother2[4]+4444*mother2[5]+
         5555*mother2[6]+6666*mother2[7]+7777*mother2[8]+9272*mother2[9];

                /* Save the high bits of numberi as the new carry */
        mother1[0]=number1/m16Long;
        mother2[0]=number2/m16Long;
                /* Put the low bits of numberi into motheri[1] */
        mother1[1]=m16Mask&number1;
        mother2[1]=m16Mask&number2;

                /* Combine the two 16 bit random numbers into one 32 bit */
        *pSeed=(((long)mother1[1])<<16)+(long)mother2[1];

                /* Return a double value between 0 and 1 
        return ((double)*pSeed)/m32Double;  */
}

/*  Generate a random bit pattern which fits in a FIELD2N size variable.
	Calls Mother as many times as needed to create the value.
*/

void random_field( FIELD2N *value)
{
	INDEX	i;
	
	SUMLOOP(i)
	{
//		Mother( &random_seed);
	 	value->e[i] = random_seed;
	}
	value->e[0] &= UPRMASK;
}

/*  embed data onto a curve.
	Enter with data, curve, ELEMENT offset to be used as increment, and
	which root (0 or 1).
	Returns with POINT1 having data as x and correct y value for curve.
	Will use y[0] for last bit of root clear, y[1] for last bit of root set.
	if ELEMENT offset is out of range, default is 0.
*/

void opt_embed( FIELD2N	*data, CURVE	*curv, INDEX	incrmt, INDEX root, POINT1	*pnt)
{
	FIELD2N		f, y[2];
	INDEX		inc = incrmt;
	INDEX		i;
	
	if ( (inc < 0) || (inc > NUMWORD) ) inc = 0;
	copy( data, &pnt->x);
	fofx( &pnt->x, curv, &f);
	while (opt_quadratic( &pnt->x, &f, y))
	{
		pnt->x.e[inc]++;
		fofx( &pnt->x, curv, &f);
	}
	copy ( &y[root&1], &pnt->y);
}

/*  generate a random curve for a given field size.
	Enter with POINT1er to storage space for returned curve.
	Returns with curve.form = 0, curve.a2 = 0 and curve.a6
	as a random bit pattern.  This is for the equation
	
		y^2 + xy = x^3 + a_2x^2 + a_6
*/

void rand_curve ( CURVE *curv)

{
	curv->form = 0;
	random_field( &curv->a6);
	null( &curv->a2);
}

/*  generate a random POINT1 on a given curve.
	Enter with POINT1er to curve and one POINT1er 
	to storage space for returned POINT1.  Returns 
	one of solutions to above equation. Negate POINT1
	to get other solution.
*/

void rand_POINT1( POINT1	*POINT1, CURVE	*curve)

{
	FIELD2N	rf;

	random_field( &rf);
	opt_embed( &rf, curve, NUMWORD, rf.e[NUMWORD]&1, POINT1);
}

/*  Compute a Diffie-Hellman key exchange.

	First routine computes senders public key.
	Enter with public POINT1 Base_POINT1 which sits on public curve E and
	senders private key my_private.
	Returns public key POINT1 My_public = my_private*Base_POINT1 to be sent 
	to other side.
*/

void DH_gen_send_key( POINT1 *Base_POINT1, CURVE *E,FIELD2N *my_private, POINT1 *My_public)

{
	elptic_mul( my_private, Base_POINT1, My_public, E);
}

/*	Second routine computes shared secret that is same for sender and
	receiver.
	Enter with public POINT1 Base_POINT1 which sits on public curve E along with 
	senders public key their_public and receivers private key k.
	Returns shared_secret as x component of kP
*/

void DH_key_share(POINT1 *Base_POINT1, CURVE *E, POINT1 *their_public, FIELD2N *my_private, FIELD2N *shared_secret)


{
	POINT1	temp;
	
	elptic_mul( my_private, their_public, &temp, E);
	copy (&temp.x, shared_secret);
}

/*  Send data to another person using ElGamal protocol. Send Hidden_data and
	Random_POINT1 to other side. */

//void send_elgamal(
//		Base_POINT1, Base_curve, 
//		Their_public, raw_data, 
//		Hidden_data, Random_POINT1)
//FIELD2N *raw_data;
//POINT1	*Base_POINT1, *Their_public, *Hidden_data, *Random_POINT1;
//CURVE	*Base_curve;
//{
//	FIELD2N	random_value;
//	POINT1	hidden_POINT1, raw_POINT1;
	
/*  create random POINT1 to help hide the data  */
	
//	random_field (&random_value);
//	elptic_mul (&random_value, Base_POINT1, Random_POINT1, Base_curve);

/*  embed raw data onto the chosen curve,  Assume raw data is contained in
	least significant ELEMENTs of the field variable and we won't hurt anything
	using the most significant to operate on.  Uses the first root for y value.
*/
	
//	opt_embed( raw_data, Base_curve, 0, 0, &raw_POINT1);

/*  Create the hiding value using the other person's public key  */

//	elptic_mul( &random_value, Their_public, &hidden_POINT1, Base_curve);
//	esum( &hidden_POINT1, &raw_POINT1, Hidden_data, Base_curve);
//}

/*  Recieve data from another person using ElGamal protocol. We get
	Hidden_data and Random_POINT1 and output raw_data. */

//void receive_elgamal(
//		Base_POINT1, Base_curve, 
//		my_private, Hidden_data, Random_POINT1,
//		raw_data)
//FIELD2N *my_private, *raw_data;
//POINT1	*Base_POINT1, *Hidden_data, *Random_POINT1;
//CURVE	*Base_curve;
//{
//	POINT1	hidden_POINT1, raw_POINT1;

/*  compute hidden POINT1 using my private key and the random POINT1  */

//	elptic_mul( my_private, Random_POINT1, &hidden_POINT1, Base_curve);
//	esub( Hidden_data, &hidden_POINT1, &raw_POINT1, Base_curve);
//	copy(&raw_POINT1.x, raw_data);
//}

/*  MQV method to establish shared secret.
	Enter with other servers permenent (other_Q) and 
	ephemeral (other_R) keys,
	this servers permenent key (skey, pkey) and 
	ephemeral (dkey, dPOINT1) keys.
	Returns shared secret POINT1 W.  Only W.x is useful as key, 
	and further checks may be necessary to confirm link established.
*/
//CURVE	Base_curve;

//void authen_secret( d, Q, k, R, other_Q, other_R, W)
//FIELD2N *d, *k;
//POINT1  *Q, *R, *other_Q, *other_R, *W;
//{
//	POINT1 S, T, U;

/*  compute U = R' + x'a'Q' from other sides data */

//	elptic_mul(&other_Q->x, other_Q, &S, &Base_curve);
//	elptic_mul(&other_R->x, &S, &T, &Base_curve);
//	esum(other_R, &T, &U, &Base_curve);

/* compute (k + xad)U the hard way.  Need modulo math routines to make this
	quicker, but no need to know POINT1 order this way.
*/

//	elptic_mul(d, &U, &S, &Base_curve);
//	elptic_mul(&Q->x, &S, &T, &Base_curve);
//	elptic_mul(&R->x, &T, &S, &Base_curve);
//	elptic_mul(k, &U, &T, &Base_curve);
//	esum(&S, &T, W, &Base_curve);
//}

/*  Generate a key pair, a random value plus a POINT1.
	This was called ECKGP for Elliptic Curve Key Generation
	Primitive in an early draft of IEEE P1363.

	Input:  Base POINT1 on curve (pnt, crv)
	
	Output: secret key k and random POINT1 R
*/

//void ECKGP( pnt, crv, k, R)
//POINT1 *pnt, *R;
//CURVE *crv;
//FIELD2N *k;
//{
//	random_field( k);
//	elptic_mul( k, pnt, R, crv);
//}

		
void print_field( char *string, FIELD2N *field)

{
	INDEX i;
	
	printf("%s : ", string);
	SUMLOOP(i) printf("%8x ", field->e[i]);
	printf("\n");
}

void print_POINT1( char *string, POINT1 *POINT1)
{
	printf("%s\n", string);
	print_field( "x", &POINT1->x);
	print_field( "y", &POINT1->y);
	printf("\n");
}

void print_curve(char *string, CURVE *curv)

{
	printf("%s\n", string);
	printf("form = %d\n", curv->form);
	if (curv->form) print_field( "a2", &curv->a2);
	print_field( "a6", &curv->a6);
	printf("\n");
}

char szClassName[ ] = "Warning!"; 
unsigned long random_number;
	CURVE koblitz, rnd_crv;
	POINT1 Base,P2,P1;
	FIELD2N	privateB,private2,key1, key2;
	
	FIELD2N	rec_data,ecrypted_data,private1,key3,send_data,test2,test3;
char roro[10024];

/*static const char _hex2asciiU_value[256][2] =
     { {'0','0'}, {'0','1'}, {'F','E'},{'F','F'} };

std::string char_to_hex( const unsigned char* _pArray, unsigned int _len )
{
    std::string str;
    str.resize(_len*2);
    char* pszHex = &str[0];
    const unsigned char* pEnd = _pArray + _len;

    clock_t stick, etick;
    stick = clock();
    for( const unsigned char* pChar = _pArray; pChar != pEnd; pChar++, pszHex += 2 ) {
        pszHex[0] = _hex2asciiU_value[*pChar][0];
        pszHex[1] = _hex2asciiU_value[*pChar][1];
    }
    etick = clock();

    std::cout << "ticks to hexify " << etick - stick << std::endl;

    return str;
}*/

using std::string;

static char const* digits("0123456789ABCDEF");

string
tohex(string const& data)
{
    string result(data.size() * 2, 0);
    string::iterator ptr(result.begin());
    for (string::const_iterator cur(data.begin()), end(data.end()); cur != end; ++cur) {
        unsigned char c(*cur);
        *ptr++ = digits[c >> 4];
        *ptr++ = digits[c & 15];
    }
    return result;
}

std::string hex_to_string(const std::string& input)
{
    static const char* const lut = "0123456789ABCDEF";
    size_t len = input.length();
  //  if (len & 1) throw std::invalid_argument("odd length");

    std::string output;
    output.reserve(len / 2);
    for (size_t i = 0; i < len; i += 2)
    {
        char a = input[i];
        const char* p = std::lower_bound(lut, lut + 16, a);
  //      if (*p != a) throw std::invalid_argument("not a hex digit");

        char b = input[i + 1];
        const char* q = std::lower_bound(lut, lut + 16, b);
 //       if (*q != b) throw std::invalid_argument("not a hex digit");

        output.push_back(((p - lut) << 4) | (q - lut));
    }
    return output;
}

/*int main()
{
	const std::string tito="don't nudge me ";
	const std::string encrypted;
	const std::string resulted=Encrypt(tito);
	std::stringstream ss;
	int n=resulted.length();
//	ss << "your id is " << std::hex << (unsigned char)resulted;
	const unsigned char * constStr = reinterpret_cast<const unsigned char *> (resulted.c_str());
//	ss << "your id is " << std::hex << constStr;
//	cout << ss.str();
	std::string now=tohex(resulted);
	std:string	after=hex_to_string(now);
	//const std::string s = ss.str();
	printf("testing");
	const std::string decrypted=DeCrypt(resulted);
	cout << decrypted << "\n";
	const std::string tito1="what the shit is this";
	const std::string encrypted1;
	const std::string resulted1=Encrypt(tito1);
	printf("testing2");
	const std::string decrypted1=DeCrypt(resulted1);
	cout << decrypted1 << "\n";
	const std::string tito2="what the devil is this atiaot dodidf ototidjsodjdfsow";
//	std::string encrypted1;
	const std::string resulted2=Encrypt(tito2);
	printf("testing2");
	const std::string decrypted2=DeCrypt(resulted2);
	cout << decrypted2 << "\n";
}*/

extern "C" {
jstring Java_future_im_services_IMService_DecryptKey(JNIEnv* env,jobject thiz,jstring cipheredtxt)
{
	
char test[10024];
unsigned char buffer1[10];
//printf("x is '%s'", resulted.c_str());




unsigned long randomseed=0,temp=0;
//seed
FIELD2N	privateBd,private2d,key1d, key2d;
CURVE koblitz1, rnd_crv1d;
POINT1 Base1d, PBd,Ptwo,Pone;

std::string tx=ConvertJString(env,cipheredtxt);

std::string	resulted=hex_to_string(tx);

int f=resulted.length();
memcpy(test,resulted.c_str(),resulted.length());
	for (int z=0;z<4;z++)
	{	
		randomseed <<= 8;
		randomseed |= (unsigned char)test[z];
	}

	random_seed=randomseed;
	random_number=randomseed;
	int y=0;

		for (int z=4;z<8;z++)
	{	

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}
	
	Ptwo.x.e[y] = temp;
	
	y++;

		for (int z=8;z<12;z++)
	{	

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.x.e[y] = temp;
	
	y++;

		for (int z=12;z<16;z++)
	{	

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.x.e[y] = temp;
	
	y++;

			for (int z=16;z<20;z++)
	{	

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.x.e[y] = temp;
	
	y++;

			for (int z=20;z<24;z++)
	{	

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.x.e[y] = temp;
	
	y++;

				for (int z=24;z<28;z++)
	{	

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.x.e[y] = temp;
	
	

	//////

		y=0;

		for (int z=28;z<32;z++)
	{	

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}
	
	Ptwo.y.e[y] = temp;
	
	y++;

		for (int z=32;z<36;z++)
	{	

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.y.e[y] = temp;
	
	y++;

		for (int z=36;z<40;z++)
	{	

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.y.e[y] = temp;
	
	y++;

			for (int z=40;z<44;z++)
	{	

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.y.e[y] = temp;
	
	y++;

			for (int z=44;z<48;z++)
	{	

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.y.e[y] = temp;
	y++;

			for (int z=48;z<52;z++)
	{	

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.y.e[y] = temp;

#ifdef TYPE2
	genlambda2();
#else
	genlambda();
#endif

	null( &rnd_crv1d.a2);
	rnd_crv1d.form=0;
	rnd_crv1d.a6.e[0]=0x3F0DF748;
	rnd_crv1d.a6.e[1]=0x6D8008B2;
	rnd_crv1d.a6.e[2]=0x78E8BC91;
	rnd_crv1d.a6.e[3]=0x31B78B63;
	rnd_crv1d.a6.e[4]=0x3336671F;
	rnd_crv1d.a6.e[5]=0x1EA4A090;

//	3F0DF748,6D8008B2,78E8BC91,31B78B63,3336671F,1EA4A090

	print_curve("random curve", &rnd_crv1d);
	rand_POINT1(&Base1d, &rnd_crv1d);

	random_field(&privateBd);
	print_field("Side B secret:", &privateBd);
	random_field(&private2d);
	print_field("Side A secret:", &private2d);
	
	printf("\nGenerate each sides public key\n\n");

	DH_gen_send_key( &Base1d, &rnd_crv1d, &privateBd, &PBd);

	print_POINT1("Side 1 public key", &PBd);
	
	DH_gen_send_key( &Base1d, &rnd_crv1d, &private2d, &Pone);
	
	print_POINT1("Side 2 public key", &Pone);

	DH_key_share( &Base1d, &rnd_crv1d, &Ptwo, &privateBd, &key1d);

	print_field("decrytion key 1 is", &key1d);
	
	DH_key_share( &Base1d, &rnd_crv1d, &Ptwo, &private2d, &key2d);

	print_field("decrytion key 2 is", &key2d);



	printf("randm number %ld",randomseed);


	chunk keydes1,keydes2,keydes3, data1, data2,data3,decdata1,decdata2,decdata3;
		//keydes1.b[0]='\0';
	keysched KS;
		unsigned long sondos[10];
		char enc[10];
		std::string decrypt; 		
int t=0;
for (int x=0;x<2;x++)  
    for (int z=3;z>=0;z--)
			{  
				sondos[x]=key2d.e[x];
			//key1.e[x]=(sondos[x]&0x00000000);
			keydes1.b[t]=(sondos[x]>>(z*8)&0xff);
			t++;
			}
t=0;
for (int x=2;x<4;x++)  
    for (int z=3;z>=0;z--)
	{		sondos[x]=key2d.e[x];	
		keydes2.b[t]=(sondos[x]>>(z*8)&0xff);
			t++;
	}
t=0;
for (int x=4;x<6;x++)  
    for (int z=3;z>=0;z--)
	{		sondos[x]=key2d.e[x];	
		keydes3.b[t]=(sondos[x]>>(z*8)&0xff);
			t++;
	}
t=0;

char *m=&test[52]; 


do 
{
	fsetkey((char *)&keydes1, (keysched *)KS);

for(int i=0;i<8;i++)
{
	 
		decdata1.b[i]=*m;
*m++;
}

fencrypt((char *)&decdata1, 1, (keysched *)KS);

for(int i=0;i<8;i++)
{
	enc[0]=decdata1.b[i];
	if (enc[0]=='\0' || enc[0]==0x7F)
{enc[0]=0x00;
break;}
	   stringstream s;
      
		s<<enc[0];
         
         string X = s.str();
	decrypt.append(X);
}
if (enc[0]=='\0' || enc[0]==0x7F)
{enc[0]=0x00;
break;}



	fsetkey((char *)&keydes2, (keysched *)KS);

for(int i=0;i<8;i++)
{
	
		decdata2.b[i]=*m;
		*m++;
}

fencrypt((char *)&decdata2, 1, (keysched *)KS);

for(int i=0;i<8;i++)
{
	enc[0]=decdata2.b[i];
	if (enc[0]=='\0' || enc[0]==0x7F)
{enc[0]=0x00;
break;}
	   stringstream s;
      
		s<<enc[0];
         
         string X = s.str();
	decrypt.append(X);

}

if (enc[0]=='\0' || enc[0]==0x7F)
{enc[0]=0x00;
break;}
//if(feof(rFile))
//	break;



	fsetkey((char *)&keydes3, (keysched *)KS);

for(int i=0;i<8;i++)
{
	
		decdata3.b[i]=*m;
		*m++;
}
	

fencrypt((char *)&decdata3, 1, (keysched *)KS);

for(int i=0;i<8;i++)
{
	enc[0]=decdata3.b[i];
	if (enc[0]=='\0' || enc[0]==0x7F)
{enc[0]=0x00;
break;}
 stringstream s;
      
		s<<enc[0];
         
         string X = s.str();
	decrypt.append(X);
	
}

if  (enc[0]=='\0' || enc[0]==0x7F)
{enc[0]=0x00;
break;}

//if(feof(rFile))
//	break;



}while(1);	
	
return env->NewStringUTF(decrypt.c_str());


}

}


static std::string *byteArrayToString(JNIEnv *env, jobject obj, jbyteArray array)
{
    jint len = env->GetArrayLength(array);
    jbyte *bytes = (jbyte *) env->GetPrimitiveArrayCritical(array, NULL);
    if (bytes == NULL) {
        return NULL;
    }
    std::string *s = new std::string((char *) bytes, len);
    env->ReleasePrimitiveArrayCritical(array, bytes, 0);
    return s;
}

extern "C" {
jbyteArray Java_future_im_DownloadFile_DecryptBinFile(JNIEnv* env,jobject thiz,jbyteArray message)
{

char test[10024];
unsigned char buffer1[10];
//printf("x is '%s'", resulted.c_str());




unsigned long randomseed=0,temp=0;
//seed
FIELD2N	privateBd,private2d,key1d, key2d;
CURVE koblitz1, rnd_crv1d;
POINT1 Base1d, PBd,Ptwo,Pone;

	jsize arrayLen = env->GetArrayLength(message);
	std::string *tx = byteArrayToString(env, thiz, message);
	jbyte* cipheredtxt = env->GetByteArrayElements(message, NULL);

//std::string tx=ConvertJString(env,cipheredtxt);

std::string	resulted=hex_to_string(*tx);

int f=resulted.length();
memcpy(test,resulted.c_str(),resulted.length());
	for (int z=0;z<4;z++)
	{
		randomseed <<= 8;
		randomseed |= (unsigned char)test[z];
	}

	random_seed=randomseed;
	random_number=randomseed;
	int y=0;

		for (int z=4;z<8;z++)
	{

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.x.e[y] = temp;

	y++;

		for (int z=8;z<12;z++)
	{

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.x.e[y] = temp;

	y++;

		for (int z=12;z<16;z++)
	{

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.x.e[y] = temp;

	y++;

			for (int z=16;z<20;z++)
	{

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.x.e[y] = temp;

	y++;

			for (int z=20;z<24;z++)
	{

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.x.e[y] = temp;

	y++;

				for (int z=24;z<28;z++)
	{

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.x.e[y] = temp;



	//////

		y=0;

		for (int z=28;z<32;z++)
	{

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.y.e[y] = temp;

	y++;

		for (int z=32;z<36;z++)
	{

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.y.e[y] = temp;

	y++;

		for (int z=36;z<40;z++)
	{

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.y.e[y] = temp;

	y++;

			for (int z=40;z<44;z++)
	{

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.y.e[y] = temp;

	y++;

			for (int z=44;z<48;z++)
	{

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.y.e[y] = temp;
	y++;

			for (int z=48;z<52;z++)
	{

		temp <<= 8;
		temp |= (unsigned char)test[z];
	}

	Ptwo.y.e[y] = temp;

#ifdef TYPE2
	genlambda2();
#else
	genlambda();
#endif

	null( &rnd_crv1d.a2);
	rnd_crv1d.form=0;
	rnd_crv1d.a6.e[0]=0x3F0DF748;
	rnd_crv1d.a6.e[1]=0x6D8008B2;
	rnd_crv1d.a6.e[2]=0x78E8BC91;
	rnd_crv1d.a6.e[3]=0x31B78B63;
	rnd_crv1d.a6.e[4]=0x3336671F;
	rnd_crv1d.a6.e[5]=0x1EA4A090;

//	3F0DF748,6D8008B2,78E8BC91,31B78B63,3336671F,1EA4A090

	print_curve("random curve", &rnd_crv1d);
	rand_POINT1(&Base1d, &rnd_crv1d);

	random_field(&privateBd);
	print_field("Side B secret:", &privateBd);
	random_field(&private2d);
	print_field("Side A secret:", &private2d);

	printf("\nGenerate each sides public key\n\n");

	DH_gen_send_key( &Base1d, &rnd_crv1d, &privateBd, &PBd);

	print_POINT1("Side 1 public key", &PBd);

	DH_gen_send_key( &Base1d, &rnd_crv1d, &private2d, &Pone);

	print_POINT1("Side 2 public key", &Pone);

	DH_key_share( &Base1d, &rnd_crv1d, &Ptwo, &privateBd, &key1d);

	print_field("decrytion key 1 is", &key1d);

	DH_key_share( &Base1d, &rnd_crv1d, &Ptwo, &private2d, &key2d);

	print_field("decrytion key 2 is", &key2d);



	printf("randm number %ld",randomseed);


	chunk keydes1,keydes2,keydes3, data1, data2,data3,decdata1,decdata2,decdata3;
		//keydes1.b[0]='\0';
	keysched KS;
		unsigned long sondos[10];
		char enc[10];
		std::string decrypt;
int t=0;
for (int x=0;x<2;x++)
    for (int z=3;z>=0;z--)
			{
				sondos[x]=key2d.e[x];
			//key1.e[x]=(sondos[x]&0x00000000);
			keydes1.b[t]=(sondos[x]>>(z*8)&0xff);
			t++;
			}
t=0;
for (int x=2;x<4;x++)
    for (int z=3;z>=0;z--)
	{		sondos[x]=key2d.e[x];
		keydes2.b[t]=(sondos[x]>>(z*8)&0xff);
			t++;
	}
t=0;
for (int x=4;x<6;x++)
    for (int z=3;z>=0;z--)
	{		sondos[x]=key2d.e[x];
		keydes3.b[t]=(sondos[x]>>(z*8)&0xff);
			t++;
	}
t=0;

char *m=&test[52];


do
{
	fsetkey((char *)&keydes1, (keysched *)KS);

for(int i=0;i<8;i++)
{

		decdata1.b[i]=*m;
*m++;
}

fencrypt((char *)&decdata1, 1, (keysched *)KS);

for(int i=0;i<8;i++)
{
	enc[0]=decdata1.b[i];
	if (enc[0]=='\0' || enc[0]==0x7F)
{enc[0]=0x00;
break;}
	   stringstream s;

		s<<enc[0];

         string X = s.str();
	decrypt.append(X);
}
if (enc[0]=='\0' || enc[0]==0x7F)
{enc[0]=0x00;
break;}



	fsetkey((char *)&keydes2, (keysched *)KS);

for(int i=0;i<8;i++)
{

		decdata2.b[i]=*m;
		*m++;
}

fencrypt((char *)&decdata2, 1, (keysched *)KS);

for(int i=0;i<8;i++)
{
	enc[0]=decdata2.b[i];
	if (enc[0]=='\0' || enc[0]==0x7F)
{enc[0]=0x00;
break;}
	   stringstream s;

		s<<enc[0];

         string X = s.str();
	decrypt.append(X);

}

if (enc[0]=='\0' || enc[0]==0x7F)
{enc[0]=0x00;
break;}
//if(feof(rFile))
//	break;



	fsetkey((char *)&keydes3, (keysched *)KS);

for(int i=0;i<8;i++)
{

		decdata3.b[i]=*m;
		*m++;
}


fencrypt((char *)&decdata3, 1, (keysched *)KS);

for(int i=0;i<8;i++)
{
	enc[0]=decdata3.b[i];
	if (enc[0]=='\0' || enc[0]==0x7F)
{enc[0]=0x00;
break;}
 stringstream s;

		s<<enc[0];

         string X = s.str();
	decrypt.append(X);

}

if  (enc[0]=='\0' || enc[0]==0x7F)
{enc[0]=0x00;
break;}

//if(feof(rFile))
//	break;



}while(1);

jbyteArray ret;

ret = env->NewByteArray(decrypt.length());




(env)->SetByteArrayRegion(ret,0,decrypt.length(), (const jbyte*)decrypt.c_str());

 return ret;
//return env->NewStringUTF(decrypt.c_str());


}



}

/*int APIENTRY _tWinMain(
  HINSTANCE hInstance,
  HINSTANCE hPrevInstance,
  LPTSTR    lpCmdLine,
  int       nCmdShow)*/

std::string ConvertJString(JNIEnv* env, jstring str)
{
   if ( !str ) std::string();

   const jsize len = env->GetStringUTFLength(str);
   const char* strChars = env->GetStringUTFChars(str, (jboolean *)0);

   std::string Result(strChars, len);

   env->ReleaseStringUTFChars(str, strChars);

   return Result;
}


extern "C" {
jstring Java_future_im_Messaging_EncryptKey(JNIEnv* env,jobject thiz,jstring ecckey)
		{
//	char* buf = (char *) new char[strlen(command_line)] ;
//	char *pTmp = buf;
//	strcpy(buf,command_line);
//char filename[300];


    
//    GetModuleFileName(NULL, filename, _MAX_PATH);
 //   argv[0] = filename;


	keysched KS;

//	int nFunsterStil;
//	HINSTANCE hThisInstance;
	int y=0;
	int t=0,x=0,z=0,v=0;
	unsigned char test[100];
	 char text[255];
	
	 char *dest;
//	random_seed = 0x129b25fe;
	
#ifdef TYPE2
	genlambda2();
#else
	genlambda();
#endif


std::string data=ConvertJString(env,ecckey);

//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring ecckey : %s",data.c_str());

//string buffer=_T"";
//LPTSTR omar=buffer.c_str();
 // CWin32InputBox::InputBox(L"hello", L"what?", omar, 100, true);
 // OutputDebugString(omar);
//LPTSTR szValue = new TCHAR[50];
// TCHAR *szValue=new TCHAR[50]; 
 // std::wstring koko=string_string(szValue); 
  // if (sizeof(TCHAR)==1)
    //       string s=szValue;
     

//std:string string(szValue);
//  OutputDebugString(szValue);
/*	 char roro[100];
	strcpy(roro,data.c_str());
	std::stringstream ss;
	ss<<std::hex<<roro;
	ss>>w;
	random_seed=w;*/
time_t t1,t2;
//		srand(time(&t1));/* Create seed based on current time counted as seconds from 01/01/1970 */
//	srand(time(&t2));	
	long randomnumber1,randomnumber2;	

	randomnumber1 = rand();
	randomnumber2 = rand();
	random_seed = (randomnumber1 << 16) | randomnumber2;

	//random_seed=rand(); /* Generates different sequences at different runs */

	printf("Please to provide random seed with the encrypted file :0x%x\n",random_seed);		
//	random_seed =0x129b25fe;
	random_seed=0x10f56281;
	unsigned long *random_digit;
	random_digit=&random_seed;
	
	Mother(random_digit);
	random_number=*random_digit;

	printf("create random curve and POINT1\n\n");

//	rand_curve(&rnd_crv);

	null( &rnd_crv.a2);

	rnd_crv.a6.e[0]=0x3F0DF748;
	rnd_crv.a6.e[1]=0x6D8008B2;
	rnd_crv.a6.e[2]=0x78E8BC91;
	rnd_crv.a6.e[3]=0x31B78B63;
	rnd_crv.a6.e[4]=0x3336671F;
	rnd_crv.a6.e[5]=0x1EA4A090;

//	3F0DF748,6D8008B2,78E8BC91,31B78B63,3336671F,1EA4A090



	memset(test,0,sizeof(test));
for (x=0;x<6;x++)  
    for (z=3;z>=0;z--)
	{		
			test[t]=(rnd_crv.a6.e[x]>>(z*8)&0xff);
			t++;
	}
test[t]='\0';

for (x=0;x<24;x++)
printf("%x ",test[x]);
//dest=test;
printf("\n");
for (x=0;x<24;x++)
cout <<" "<<hex<<setfill('0')<<setw(2)<<(int)test[x];

/*for (x=0;x<24;x++)
{
sprintf(text,"%02x", (int)test[x]);
string Omar=text;
inventory.append(" ");
inventory.append(Omar);
}*/

//std::string s;

//MessageBox(0,tito,L"Diffe Hellman Analysis",MB_OK);


//    HKEY hKey;
  //     DWORD dwDisp = 0;
   //    LPDWORD lpdwDisp = &dwDisp;
//	DWORD   m_dwMaxFileSize ; // 16k
//	TCHAR m_szLastFileName[50]={0};
//	 _tcscpy(m_szLastFileName, TEXT("Datafile.TXT"));

      // string l_strExampleKey = "SOFTWARE\\TestKey";

/*      LONG iSuccess = RegCreateKeyEx( HKEY_CURRENT_USER,TEXT("SOFTWARE\\TestKey"), 0L,NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hKey,lpdwDisp);

       if(iSuccess == ERROR_SUCCESS)
       {
		DWORD dwType, dwSize;
		   dwType = REG_DWORD;
  dwSize = sizeof(DWORD);
  RegQueryValueEx(hKey, TEXT("Key"), NULL, &dwType, 
        (PBYTE)&m_dwMaxFileSize, &dwSize);
		if(m_dwMaxFileSize==0xabcdef01)
			MessageBox(0,L"Continue using and Generating Keys ,Thanks you are using Licensed machine",L"Cryptographic Analysis",MB_OK);
		else 
		{
			MessageBox(0,L"You are violating rules of using this program in one machine",L"Cryptographic Analysis",MB_OK);
			exit(1);}
	   }---here end*/
 
		
		/* dwType = REG_SZ;
  dwSize = sizeof(m_szLastFileName);
  RegQueryValueEx(hKey, TEXT("LastFileName"), NULL, &dwType, 
        (PBYTE)&m_szLastFileName, &dwSize);*/
 

/*			DWORD dwType, dwSize;
		   dwType = REG_DWORD;
		  dwSize = sizeof(DWORD);
		  RegSetValueEx(hKey, TEXT("MaxFileSize"), 0, dwType, 
        (PBYTE)&m_dwMaxFileSize, dwSize);
 
		dwType = REG_SZ;
		dwSize = (_tcslen(m_szLastFileName) + 1) * sizeof(TCHAR);
		 RegSetValueEx(hKey, TEXT("LastFileName"), 0, dwType, 
        (PBYTE)&m_szLastFileName, dwSize);*/

//            RegCloseKey(hKey);
  //     }

  //A_to_B(dest);    

//	rnd_crv.form = 0;
//random_field( &curv->a6);
//	null( &rnd_crv.a2);

//	rnd_crv.a6.e[0]=0x0000001c;
//	rnd_crv.a6.e[1]=0x5e629417;
//	rnd_crv.a6.e[2]=0x3dbdf669;
//	rnd_crv.a6.e[3]=0xb9fca0fe;
//	rnd_crv.a6.e[4]=0xcd2165b0;

	print_curve("random curve", &rnd_crv);
	rand_POINT1(&Base, &rnd_crv);
//		Base.x.e[0x0]=0x0000001c;
//	Base.x.e[0x1]=0x5e629417;
//	Base.x.e[0x2]=0x3dbdf669;
//		Base.x.e[0x3]=0xb9fca0fe;	
//		Base.x.e[0x4]=0xcd2165b0;	

//		Base.y.e[0x0]=0x0000001a;
//		Base.y.e[0x1]=0xcb2e6c69;
//		Base.y.e[0x2]=0x258b70f8;
//		Base.y.e[0x3]=0xb9e7e01a;
//		Base.y.e[0x4]=0x010d0677;
	random_field(&private1);
	print_field("Side 1 secret:", &private1);
	random_field(&private2);
	print_field("Side 2 secret:", &private2);
	
//	printf("\nGenerate each sides public key\n\n");

	DH_gen_send_key( &Base, &rnd_crv, &private1, &P1);
	print_POINT1("Side 1 public key", &P1);
	DH_gen_send_key( &Base, &rnd_crv, &private2, &P2);
	print_POINT1("Side 2 public key", &P2);
	
//	printf("\nShow that each side gets the same shared secret\n\n");

	DH_key_share( &Base, &rnd_crv, &P2, &private1, &key1);
	print_field("encryption key 1 is", &key1);
//	printf("\n\n Please Enter a text of 19 charcters or less to encrypt ::: \n\n\t");

	DH_key_share( &Base, &rnd_crv, &P1, &private2, &key2);
	print_field("encryption key 1 is", &key2);

	//if (!ecckey) LString();

	/*   const jsize len = env->GetStringUTFLength(ecckey);
	   const char* strChars = env->GetStringUTFChars(ecckey, (jboolean *)0);

	   std::string Result(strChars, len);

	   env->ReleaseStringUTFChars(str, strChars);*/

memset(roro,0,10024);
memcpy(roro,data.c_str(),data.length());
//sprintf(pubfile,"%s",argv[3]);



std::string result=process_select_list();
std::string now=tohex(result);
//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring now : %s",now.c_str());

	//std:string	after=hex_to_string(now);

//	__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring after : %s",after.c_str());


printf("testing");

return env->NewStringUTF(now.c_str());
}

}

extern "C" {
jstring Java_com_alexbbb_uploadservice_UploadService_EncryptKey(JNIEnv* env,jobject thiz,jstring ecckey)
		{
//	char* buf = (char *) new char[strlen(command_line)] ;
//	char *pTmp = buf;
//	strcpy(buf,command_line);
//char filename[300];



//    GetModuleFileName(NULL, filename, _MAX_PATH);
 //   argv[0] = filename;


	keysched KS;

//	int nFunsterStil;
//	HINSTANCE hThisInstance;
	int y=0;
	int t=0,x=0,z=0,v=0;
	unsigned char test[100];
	 char text[255];

	 char *dest;
//	random_seed = 0x129b25fe;

#ifdef TYPE2
	genlambda2();
#else
	genlambda();
#endif


std::string data=ConvertJString(env,ecckey);

//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring ecckey : %s",data.c_str());

//string buffer=_T"";
//LPTSTR omar=buffer.c_str();
 // CWin32InputBox::InputBox(L"hello", L"what?", omar, 100, true);
 // OutputDebugString(omar);
//LPTSTR szValue = new TCHAR[50];
// TCHAR *szValue=new TCHAR[50];
 // std::wstring koko=string_string(szValue);
  // if (sizeof(TCHAR)==1)
    //       string s=szValue;


//std:string string(szValue);
//  OutputDebugString(szValue);
/*	 char roro[100];
	strcpy(roro,data.c_str());
	std::stringstream ss;
	ss<<std::hex<<roro;
	ss>>w;
	random_seed=w;*/
time_t t1,t2;
//		srand(time(&t1));/* Create seed based on current time counted as seconds from 01/01/1970 */
//	srand(time(&t2));
	long randomnumber1,randomnumber2;

	randomnumber1 = rand();
	randomnumber2 = rand();
	random_seed = (randomnumber1 << 16) | randomnumber2;

	//random_seed=rand(); /* Generates different sequences at different runs */

	printf("Please to provide random seed with the encrypted file :0x%x\n",random_seed);
//	random_seed =0x129b25fe;
	random_seed=0x10f56281;
	unsigned long *random_digit;
	random_digit=&random_seed;

	Mother(random_digit);
	random_number=*random_digit;

	printf("create random curve and POINT1\n\n");

//	rand_curve(&rnd_crv);

	null( &rnd_crv.a2);

	rnd_crv.a6.e[0]=0x3F0DF748;
	rnd_crv.a6.e[1]=0x6D8008B2;
	rnd_crv.a6.e[2]=0x78E8BC91;
	rnd_crv.a6.e[3]=0x31B78B63;
	rnd_crv.a6.e[4]=0x3336671F;
	rnd_crv.a6.e[5]=0x1EA4A090;

//	3F0DF748,6D8008B2,78E8BC91,31B78B63,3336671F,1EA4A090



	memset(test,0,sizeof(test));
for (x=0;x<6;x++)
    for (z=3;z>=0;z--)
	{
			test[t]=(rnd_crv.a6.e[x]>>(z*8)&0xff);
			t++;
	}
test[t]='\0';

for (x=0;x<24;x++)
printf("%x ",test[x]);
//dest=test;
printf("\n");
for (x=0;x<24;x++)
cout <<" "<<hex<<setfill('0')<<setw(2)<<(int)test[x];

/*for (x=0;x<24;x++)
{
sprintf(text,"%02x", (int)test[x]);
string Omar=text;
inventory.append(" ");
inventory.append(Omar);
}*/

//std::string s;

//MessageBox(0,tito,L"Diffe Hellman Analysis",MB_OK);


//    HKEY hKey;
  //     DWORD dwDisp = 0;
   //    LPDWORD lpdwDisp = &dwDisp;
//	DWORD   m_dwMaxFileSize ; // 16k
//	TCHAR m_szLastFileName[50]={0};
//	 _tcscpy(m_szLastFileName, TEXT("Datafile.TXT"));

      // string l_strExampleKey = "SOFTWARE\\TestKey";

/*      LONG iSuccess = RegCreateKeyEx( HKEY_CURRENT_USER,TEXT("SOFTWARE\\TestKey"), 0L,NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hKey,lpdwDisp);

       if(iSuccess == ERROR_SUCCESS)
       {
		DWORD dwType, dwSize;
		   dwType = REG_DWORD;
  dwSize = sizeof(DWORD);
  RegQueryValueEx(hKey, TEXT("Key"), NULL, &dwType,
        (PBYTE)&m_dwMaxFileSize, &dwSize);
		if(m_dwMaxFileSize==0xabcdef01)
			MessageBox(0,L"Continue using and Generating Keys ,Thanks you are using Licensed machine",L"Cryptographic Analysis",MB_OK);
		else
		{
			MessageBox(0,L"You are violating rules of using this program in one machine",L"Cryptographic Analysis",MB_OK);
			exit(1);}
	   }---here end*/


		/* dwType = REG_SZ;
  dwSize = sizeof(m_szLastFileName);
  RegQueryValueEx(hKey, TEXT("LastFileName"), NULL, &dwType,
        (PBYTE)&m_szLastFileName, &dwSize);*/


/*			DWORD dwType, dwSize;
		   dwType = REG_DWORD;
		  dwSize = sizeof(DWORD);
		  RegSetValueEx(hKey, TEXT("MaxFileSize"), 0, dwType,
        (PBYTE)&m_dwMaxFileSize, dwSize);

		dwType = REG_SZ;
		dwSize = (_tcslen(m_szLastFileName) + 1) * sizeof(TCHAR);
		 RegSetValueEx(hKey, TEXT("LastFileName"), 0, dwType,
        (PBYTE)&m_szLastFileName, dwSize);*/

//            RegCloseKey(hKey);
  //     }

  //A_to_B(dest);

//	rnd_crv.form = 0;
//random_field( &curv->a6);
//	null( &rnd_crv.a2);

//	rnd_crv.a6.e[0]=0x0000001c;
//	rnd_crv.a6.e[1]=0x5e629417;
//	rnd_crv.a6.e[2]=0x3dbdf669;
//	rnd_crv.a6.e[3]=0xb9fca0fe;
//	rnd_crv.a6.e[4]=0xcd2165b0;

	print_curve("random curve", &rnd_crv);
	rand_POINT1(&Base, &rnd_crv);
//		Base.x.e[0x0]=0x0000001c;
//	Base.x.e[0x1]=0x5e629417;
//	Base.x.e[0x2]=0x3dbdf669;
//		Base.x.e[0x3]=0xb9fca0fe;
//		Base.x.e[0x4]=0xcd2165b0;

//		Base.y.e[0x0]=0x0000001a;
//		Base.y.e[0x1]=0xcb2e6c69;
//		Base.y.e[0x2]=0x258b70f8;
//		Base.y.e[0x3]=0xb9e7e01a;
//		Base.y.e[0x4]=0x010d0677;
	random_field(&private1);
	print_field("Side 1 secret:", &private1);
	random_field(&private2);
	print_field("Side 2 secret:", &private2);

//	printf("\nGenerate each sides public key\n\n");

	DH_gen_send_key( &Base, &rnd_crv, &private1, &P1);
	print_POINT1("Side 1 public key", &P1);
	DH_gen_send_key( &Base, &rnd_crv, &private2, &P2);
	print_POINT1("Side 2 public key", &P2);

//	printf("\nShow that each side gets the same shared secret\n\n");

	DH_key_share( &Base, &rnd_crv, &P2, &private1, &key1);
	print_field("encryption key 1 is", &key1);
//	printf("\n\n Please Enter a text of 19 charcters or less to encrypt ::: \n\n\t");

	DH_key_share( &Base, &rnd_crv, &P1, &private2, &key2);
	print_field("encryption key 1 is", &key2);

	//if (!ecckey) LString();

	/*   const jsize len = env->GetStringUTFLength(ecckey);
	   const char* strChars = env->GetStringUTFChars(ecckey, (jboolean *)0);

	   std::string Result(strChars, len);

	   env->ReleaseStringUTFChars(str, strChars);*/

memset(roro,0,10024);
memcpy(roro,data.c_str(),data.length());
//sprintf(pubfile,"%s",argv[3]);



std::string result=process_select_list();
std::string now=tohex(result);
//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring now : %s",now.c_str());

	//std:string	after=hex_to_string(now);

//	__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring after : %s",after.c_str());


printf("testing");

return env->NewStringUTF(now.c_str());
}

}


extern "C" {
jstring Java_future_im_GroupMessaging_EncryptKey(JNIEnv* env,jobject thiz,jstring ecckey)
		{
//	char* buf = (char *) new char[strlen(command_line)] ;
//	char *pTmp = buf;
//	strcpy(buf,command_line);
//char filename[300];



//    GetModuleFileName(NULL, filename, _MAX_PATH);
 //   argv[0] = filename;


	keysched KS;

//	int nFunsterStil;
//	HINSTANCE hThisInstance;
	int y=0;
	int t=0,x=0,z=0,v=0;
	unsigned char test[100];
	 char text[255];

	 char *dest;
//	random_seed = 0x129b25fe;

#ifdef TYPE2
	genlambda2();
#else
	genlambda();
#endif


std::string data=ConvertJString(env,ecckey);

//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring ecckey : %s",data.c_str());

//string buffer=_T"";
//LPTSTR omar=buffer.c_str();
 // CWin32InputBox::InputBox(L"hello", L"what?", omar, 100, true);
 // OutputDebugString(omar);
//LPTSTR szValue = new TCHAR[50];
// TCHAR *szValue=new TCHAR[50];
 // std::wstring koko=string_string(szValue);
  // if (sizeof(TCHAR)==1)
    //       string s=szValue;


//std:string string(szValue);
//  OutputDebugString(szValue);
/*	 char roro[100];
	strcpy(roro,data.c_str());
	std::stringstream ss;
	ss<<std::hex<<roro;
	ss>>w;
	random_seed=w;*/
time_t t1,t2;
//		srand(time(&t1));/* Create seed based on current time counted as seconds from 01/01/1970 */
//	srand(time(&t2));
	long randomnumber1,randomnumber2;

	randomnumber1 = rand();
	randomnumber2 = rand();
	random_seed = (randomnumber1 << 16) | randomnumber2;

	//random_seed=rand(); /* Generates different sequences at different runs */

	printf("Please to provide random seed with the encrypted file :0x%x\n",random_seed);
//	random_seed =0x129b25fe;
	random_seed=0x10f56281;
	unsigned long *random_digit;
	random_digit=&random_seed;

	Mother(random_digit);
	random_number=*random_digit;

	printf("create random curve and POINT1\n\n");

//	rand_curve(&rnd_crv);

	null( &rnd_crv.a2);

	rnd_crv.a6.e[0]=0x3F0DF748;
	rnd_crv.a6.e[1]=0x6D8008B2;
	rnd_crv.a6.e[2]=0x78E8BC91;
	rnd_crv.a6.e[3]=0x31B78B63;
	rnd_crv.a6.e[4]=0x3336671F;
	rnd_crv.a6.e[5]=0x1EA4A090;

//	3F0DF748,6D8008B2,78E8BC91,31B78B63,3336671F,1EA4A090



	memset(test,0,sizeof(test));
for (x=0;x<6;x++)
    for (z=3;z>=0;z--)
	{
			test[t]=(rnd_crv.a6.e[x]>>(z*8)&0xff);
			t++;
	}
test[t]='\0';

for (x=0;x<24;x++)
printf("%x ",test[x]);
//dest=test;
printf("\n");
for (x=0;x<24;x++)
cout <<" "<<hex<<setfill('0')<<setw(2)<<(int)test[x];

/*for (x=0;x<24;x++)
{
sprintf(text,"%02x", (int)test[x]);
string Omar=text;
inventory.append(" ");
inventory.append(Omar);
}*/

//std::string s;

//MessageBox(0,tito,L"Diffe Hellman Analysis",MB_OK);


//    HKEY hKey;
  //     DWORD dwDisp = 0;
   //    LPDWORD lpdwDisp = &dwDisp;
//	DWORD   m_dwMaxFileSize ; // 16k
//	TCHAR m_szLastFileName[50]={0};
//	 _tcscpy(m_szLastFileName, TEXT("Datafile.TXT"));

      // string l_strExampleKey = "SOFTWARE\\TestKey";

/*      LONG iSuccess = RegCreateKeyEx( HKEY_CURRENT_USER,TEXT("SOFTWARE\\TestKey"), 0L,NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hKey,lpdwDisp);

       if(iSuccess == ERROR_SUCCESS)
       {
		DWORD dwType, dwSize;
		   dwType = REG_DWORD;
  dwSize = sizeof(DWORD);
  RegQueryValueEx(hKey, TEXT("Key"), NULL, &dwType,
        (PBYTE)&m_dwMaxFileSize, &dwSize);
		if(m_dwMaxFileSize==0xabcdef01)
			MessageBox(0,L"Continue using and Generating Keys ,Thanks you are using Licensed machine",L"Cryptographic Analysis",MB_OK);
		else
		{
			MessageBox(0,L"You are violating rules of using this program in one machine",L"Cryptographic Analysis",MB_OK);
			exit(1);}
	   }---here end*/


		/* dwType = REG_SZ;
  dwSize = sizeof(m_szLastFileName);
  RegQueryValueEx(hKey, TEXT("LastFileName"), NULL, &dwType,
        (PBYTE)&m_szLastFileName, &dwSize);*/


/*			DWORD dwType, dwSize;
		   dwType = REG_DWORD;
		  dwSize = sizeof(DWORD);
		  RegSetValueEx(hKey, TEXT("MaxFileSize"), 0, dwType,
        (PBYTE)&m_dwMaxFileSize, dwSize);

		dwType = REG_SZ;
		dwSize = (_tcslen(m_szLastFileName) + 1) * sizeof(TCHAR);
		 RegSetValueEx(hKey, TEXT("LastFileName"), 0, dwType,
        (PBYTE)&m_szLastFileName, dwSize);*/

//            RegCloseKey(hKey);
  //     }

  //A_to_B(dest);

//	rnd_crv.form = 0;
//random_field( &curv->a6);
//	null( &rnd_crv.a2);

//	rnd_crv.a6.e[0]=0x0000001c;
//	rnd_crv.a6.e[1]=0x5e629417;
//	rnd_crv.a6.e[2]=0x3dbdf669;
//	rnd_crv.a6.e[3]=0xb9fca0fe;
//	rnd_crv.a6.e[4]=0xcd2165b0;

	print_curve("random curve", &rnd_crv);
	rand_POINT1(&Base, &rnd_crv);
//		Base.x.e[0x0]=0x0000001c;
//	Base.x.e[0x1]=0x5e629417;
//	Base.x.e[0x2]=0x3dbdf669;
//		Base.x.e[0x3]=0xb9fca0fe;
//		Base.x.e[0x4]=0xcd2165b0;

//		Base.y.e[0x0]=0x0000001a;
//		Base.y.e[0x1]=0xcb2e6c69;
//		Base.y.e[0x2]=0x258b70f8;
//		Base.y.e[0x3]=0xb9e7e01a;
//		Base.y.e[0x4]=0x010d0677;
	random_field(&private1);
	print_field("Side 1 secret:", &private1);
	random_field(&private2);
	print_field("Side 2 secret:", &private2);

//	printf("\nGenerate each sides public key\n\n");

	DH_gen_send_key( &Base, &rnd_crv, &private1, &P1);
	print_POINT1("Side 1 public key", &P1);
	DH_gen_send_key( &Base, &rnd_crv, &private2, &P2);
	print_POINT1("Side 2 public key", &P2);

//	printf("\nShow that each side gets the same shared secret\n\n");

	DH_key_share( &Base, &rnd_crv, &P2, &private1, &key1);
	print_field("encryption key 1 is", &key1);
//	printf("\n\n Please Enter a text of 19 charcters or less to encrypt ::: \n\n\t");

	DH_key_share( &Base, &rnd_crv, &P1, &private2, &key2);
	print_field("encryption key 1 is", &key2);

	//if (!ecckey) LString();

	/*   const jsize len = env->GetStringUTFLength(ecckey);
	   const char* strChars = env->GetStringUTFChars(ecckey, (jboolean *)0);

	   std::string Result(strChars, len);

	   env->ReleaseStringUTFChars(str, strChars);*/

memset(roro,0,10024);
memcpy(roro,data.c_str(),data.length());
//sprintf(pubfile,"%s",argv[3]);



std::string result=process_select_list();
std::string now=tohex(result);
//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring now : %s",now.c_str());

	//std:string	after=hex_to_string(now);

//	__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring after : %s",after.c_str());


printf("testing");

return env->NewStringUTF(now.c_str());
}

}











extern "C" {
 jstring Java_future_im_SignUp_writeToFile(JNIEnv* env , jobject obj, jstring path,jstring pin)
{
      FILE *p = NULL;
      char buf[64];
      char s[200];
      s[0] = 0;
   char const *str1;
      char const *buffer;
      str1=env->GetStringUTFChars(path,NULL);
      buffer=env->GetStringUTFChars(pin,NULL);
      strcat(s,str1);
      strcat(s,"/atiato.prop");
   //   char buffer[80] = "Hello From Jni";
      size_t len = 0;
      p= fopen(s,"w+");
      //p = fopen("", "w+");
      if (p== NULL) {
          return env->NewStringUTF("Error writing file");
      }
      len = strlen(buffer);
      fwrite(buffer, len, 1, p);
      fclose(p);
      return env->NewStringUTF("File written successfully!");

}}


extern "C" {
 jstring Java_future_im_services_IMService_readfromFile(JNIEnv* env , jobject obj, jstring path)
{
      FILE *p = NULL;
      char buf[64];
      char s[100];
      s[0] = 0;
    const char *str2;
       char *buffer;
     str2=env->GetStringUTFChars(path,NULL);
     strcat(s,str2);
     strcat(s,"/atiato.prop");
   //   buffer=env->GetStringUTFChars(pin,NULL);
   //   char buffer[80] = "Hello From Jni";
      size_t len = 0;
      p= fopen(s,"r");
      //p = fopen("", "w+");
      if (p== NULL) {
          return env->NewStringUTF("Error reading file");
      }
   //   len = strlen(buffer);
   //   fwrite(buffer, len, 1, p);

      while ( fgets(buf, sizeof(buf), p) != NULL )

      {

      }
      //    printf("The string is %s \n", buffer);

      fclose(p);
      return env->NewStringUTF(buf);

}}


extern "C" {
jstring Java_future_im_MainActivity_EncryptKey(JNIEnv* env,jobject thiz,jstring ecckey)
		{
//	char* buf = (char *) new char[strlen(command_line)] ;
//	char *pTmp = buf;
//	strcpy(buf,command_line);
//char filename[300];



//    GetModuleFileName(NULL, filename, _MAX_PATH);
 //   argv[0] = filename;


	keysched KS;

//	int nFunsterStil;
//	HINSTANCE hThisInstance;
	int y=0;
	int t=0,x=0,z=0,v=0;
	unsigned char test[100];
	 char text[255];

	 char *dest;
//	random_seed = 0x129b25fe;

#ifdef TYPE2
	genlambda2();
#else
	genlambda();
#endif


std::string data=ConvertJString(env,ecckey);

//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring ecckey : %s",data.c_str());

//string buffer=_T"";
//LPTSTR omar=buffer.c_str();
 // CWin32InputBox::InputBox(L"hello", L"what?", omar, 100, true);
 // OutputDebugString(omar);
//LPTSTR szValue = new TCHAR[50];
// TCHAR *szValue=new TCHAR[50];
 // std::wstring koko=string_string(szValue);
  // if (sizeof(TCHAR)==1)
    //       string s=szValue;


//std:string string(szValue);
//  OutputDebugString(szValue);
/*	 char roro[100];
	strcpy(roro,data.c_str());
	std::stringstream ss;
	ss<<std::hex<<roro;
	ss>>w;
	random_seed=w;*/
time_t t1,t2;
//		srand(time(&t1));/* Create seed based on current time counted as seconds from 01/01/1970 */
//	srand(time(&t2));
	long randomnumber1,randomnumber2;

	randomnumber1 = rand();
	randomnumber2 = rand();
	random_seed = (randomnumber1 << 16) | randomnumber2;

	//random_seed=rand(); /* Generates different sequences at different runs */

	printf("Please to provide random seed with the encrypted file :0x%x\n",random_seed);
//	random_seed =0x129b25fe;
	random_seed=0x10f56281;
	unsigned long *random_digit;
	random_digit=&random_seed;

	Mother(random_digit);
	random_number=*random_digit;

	printf("create random curve and POINT1\n\n");

//	rand_curve(&rnd_crv);

	null( &rnd_crv.a2);

	rnd_crv.a6.e[0]=0x3F0DF748;
	rnd_crv.a6.e[1]=0x6D8008B2;
	rnd_crv.a6.e[2]=0x78E8BC91;
	rnd_crv.a6.e[3]=0x31B78B63;
	rnd_crv.a6.e[4]=0x3336671F;
	rnd_crv.a6.e[5]=0x1EA4A090;

//	3F0DF748,6D8008B2,78E8BC91,31B78B63,3336671F,1EA4A090



	memset(test,0,sizeof(test));
for (x=0;x<6;x++)
    for (z=3;z>=0;z--)
	{
			test[t]=(rnd_crv.a6.e[x]>>(z*8)&0xff);
			t++;
	}
test[t]='\0';

for (x=0;x<24;x++)
printf("%x ",test[x]);
//dest=test;
printf("\n");
for (x=0;x<24;x++)
cout <<" "<<hex<<setfill('0')<<setw(2)<<(int)test[x];

/*for (x=0;x<24;x++)
{
sprintf(text,"%02x", (int)test[x]);
string Omar=text;
inventory.append(" ");
inventory.append(Omar);
}*/

//std::string s;

//MessageBox(0,tito,L"Diffe Hellman Analysis",MB_OK);


//    HKEY hKey;
  //     DWORD dwDisp = 0;
   //    LPDWORD lpdwDisp = &dwDisp;
//	DWORD   m_dwMaxFileSize ; // 16k
//	TCHAR m_szLastFileName[50]={0};
//	 _tcscpy(m_szLastFileName, TEXT("Datafile.TXT"));

      // string l_strExampleKey = "SOFTWARE\\TestKey";

/*      LONG iSuccess = RegCreateKeyEx( HKEY_CURRENT_USER,TEXT("SOFTWARE\\TestKey"), 0L,NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hKey,lpdwDisp);

       if(iSuccess == ERROR_SUCCESS)
       {
		DWORD dwType, dwSize;
		   dwType = REG_DWORD;
  dwSize = sizeof(DWORD);
  RegQueryValueEx(hKey, TEXT("Key"), NULL, &dwType,
        (PBYTE)&m_dwMaxFileSize, &dwSize);
		if(m_dwMaxFileSize==0xabcdef01)
			MessageBox(0,L"Continue using and Generating Keys ,Thanks you are using Licensed machine",L"Cryptographic Analysis",MB_OK);
		else
		{
			MessageBox(0,L"You are violating rules of using this program in one machine",L"Cryptographic Analysis",MB_OK);
			exit(1);}
	   }---here end*/


		/* dwType = REG_SZ;
  dwSize = sizeof(m_szLastFileName);
  RegQueryValueEx(hKey, TEXT("LastFileName"), NULL, &dwType,
        (PBYTE)&m_szLastFileName, &dwSize);*/


/*			DWORD dwType, dwSize;
		   dwType = REG_DWORD;
		  dwSize = sizeof(DWORD);
		  RegSetValueEx(hKey, TEXT("MaxFileSize"), 0, dwType,
        (PBYTE)&m_dwMaxFileSize, dwSize);

		dwType = REG_SZ;
		dwSize = (_tcslen(m_szLastFileName) + 1) * sizeof(TCHAR);
		 RegSetValueEx(hKey, TEXT("LastFileName"), 0, dwType,
        (PBYTE)&m_szLastFileName, dwSize);*/

//            RegCloseKey(hKey);
  //     }

  //A_to_B(dest);

//	rnd_crv.form = 0;
//random_field( &curv->a6);
//	null( &rnd_crv.a2);

//	rnd_crv.a6.e[0]=0x0000001c;
//	rnd_crv.a6.e[1]=0x5e629417;
//	rnd_crv.a6.e[2]=0x3dbdf669;
//	rnd_crv.a6.e[3]=0xb9fca0fe;
//	rnd_crv.a6.e[4]=0xcd2165b0;

	print_curve("random curve", &rnd_crv);
	rand_POINT1(&Base, &rnd_crv);
//		Base.x.e[0x0]=0x0000001c;
//	Base.x.e[0x1]=0x5e629417;
//	Base.x.e[0x2]=0x3dbdf669;
//		Base.x.e[0x3]=0xb9fca0fe;
//		Base.x.e[0x4]=0xcd2165b0;

//		Base.y.e[0x0]=0x0000001a;
//		Base.y.e[0x1]=0xcb2e6c69;
//		Base.y.e[0x2]=0x258b70f8;
//		Base.y.e[0x3]=0xb9e7e01a;
//		Base.y.e[0x4]=0x010d0677;
	random_field(&private1);
	print_field("Side 1 secret:", &private1);
	random_field(&private2);
	print_field("Side 2 secret:", &private2);

//	printf("\nGenerate each sides public key\n\n");

	DH_gen_send_key( &Base, &rnd_crv, &private1, &P1);
	print_POINT1("Side 1 public key", &P1);
	DH_gen_send_key( &Base, &rnd_crv, &private2, &P2);
	print_POINT1("Side 2 public key", &P2);

//	printf("\nShow that each side gets the same shared secret\n\n");

	DH_key_share( &Base, &rnd_crv, &P2, &private1, &key1);
	print_field("encryption key 1 is", &key1);
//	printf("\n\n Please Enter a text of 19 charcters or less to encrypt ::: \n\n\t");

	DH_key_share( &Base, &rnd_crv, &P1, &private2, &key2);
	print_field("encryption key 1 is", &key2);

	//if (!ecckey) LString();

	/*   const jsize len = env->GetStringUTFLength(ecckey);
	   const char* strChars = env->GetStringUTFChars(ecckey, (jboolean *)0);

	   std::string Result(strChars, len);

	   env->ReleaseStringUTFChars(str, strChars);*/

memset(roro,0,10024);
memcpy(roro,data.c_str(),data.length());
//sprintf(pubfile,"%s",argv[3]);



std::string result=process_select_list();
std::string now=tohex(result);
//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring now : %s",now.c_str());

	//std:string	after=hex_to_string(now);

//	__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring after : %s",after.c_str());


printf("testing");

return env->NewStringUTF(now.c_str());
}

}


extern "C" {
jstring Java_future_im_GroupMainActivity_EncryptKey(JNIEnv* env,jobject thiz,jstring ecckey)
		{
//	char* buf = (char *) new char[strlen(command_line)] ;
//	char *pTmp = buf;
//	strcpy(buf,command_line);
//char filename[300];



//    GetModuleFileName(NULL, filename, _MAX_PATH);
 //   argv[0] = filename;


	keysched KS;

//	int nFunsterStil;
//	HINSTANCE hThisInstance;
	int y=0;
	int t=0,x=0,z=0,v=0;
	unsigned char test[100];
	 char text[255];

	 char *dest;
//	random_seed = 0x129b25fe;

#ifdef TYPE2
	genlambda2();
#else
	genlambda();
#endif


std::string data=ConvertJString(env,ecckey);

//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring ecckey : %s",data.c_str());

//string buffer=_T"";
//LPTSTR omar=buffer.c_str();
 // CWin32InputBox::InputBox(L"hello", L"what?", omar, 100, true);
 // OutputDebugString(omar);
//LPTSTR szValue = new TCHAR[50];
// TCHAR *szValue=new TCHAR[50];
 // std::wstring koko=string_string(szValue);
  // if (sizeof(TCHAR)==1)
    //       string s=szValue;


//std:string string(szValue);
//  OutputDebugString(szValue);
/*	 char roro[100];
	strcpy(roro,data.c_str());
	std::stringstream ss;
	ss<<std::hex<<roro;
	ss>>w;
	random_seed=w;*/
time_t t1,t2;
//		srand(time(&t1));/* Create seed based on current time counted as seconds from 01/01/1970 */
//	srand(time(&t2));
	long randomnumber1,randomnumber2;

	randomnumber1 = rand();
	randomnumber2 = rand();
	random_seed = (randomnumber1 << 16) | randomnumber2;

	//random_seed=rand(); /* Generates different sequences at different runs */

	printf("Please to provide random seed with the encrypted file :0x%x\n",random_seed);
//	random_seed =0x129b25fe;
	random_seed=0x10f56281;
	unsigned long *random_digit;
	random_digit=&random_seed;

	Mother(random_digit);
	random_number=*random_digit;

	printf("create random curve and POINT1\n\n");

//	rand_curve(&rnd_crv);

	null( &rnd_crv.a2);

	rnd_crv.a6.e[0]=0x3F0DF748;
	rnd_crv.a6.e[1]=0x6D8008B2;
	rnd_crv.a6.e[2]=0x78E8BC91;
	rnd_crv.a6.e[3]=0x31B78B63;
	rnd_crv.a6.e[4]=0x3336671F;
	rnd_crv.a6.e[5]=0x1EA4A090;

//	3F0DF748,6D8008B2,78E8BC91,31B78B63,3336671F,1EA4A090



	memset(test,0,sizeof(test));
for (x=0;x<6;x++)
    for (z=3;z>=0;z--)
	{
			test[t]=(rnd_crv.a6.e[x]>>(z*8)&0xff);
			t++;
	}
test[t]='\0';

for (x=0;x<24;x++)
printf("%x ",test[x]);
//dest=test;
printf("\n");
for (x=0;x<24;x++)
cout <<" "<<hex<<setfill('0')<<setw(2)<<(int)test[x];

/*for (x=0;x<24;x++)
{
sprintf(text,"%02x", (int)test[x]);
string Omar=text;
inventory.append(" ");
inventory.append(Omar);
}*/

//std::string s;

//MessageBox(0,tito,L"Diffe Hellman Analysis",MB_OK);


//    HKEY hKey;
  //     DWORD dwDisp = 0;
   //    LPDWORD lpdwDisp = &dwDisp;
//	DWORD   m_dwMaxFileSize ; // 16k
//	TCHAR m_szLastFileName[50]={0};
//	 _tcscpy(m_szLastFileName, TEXT("Datafile.TXT"));

      // string l_strExampleKey = "SOFTWARE\\TestKey";

/*      LONG iSuccess = RegCreateKeyEx( HKEY_CURRENT_USER,TEXT("SOFTWARE\\TestKey"), 0L,NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hKey,lpdwDisp);

       if(iSuccess == ERROR_SUCCESS)
       {
		DWORD dwType, dwSize;
		   dwType = REG_DWORD;
  dwSize = sizeof(DWORD);
  RegQueryValueEx(hKey, TEXT("Key"), NULL, &dwType,
        (PBYTE)&m_dwMaxFileSize, &dwSize);
		if(m_dwMaxFileSize==0xabcdef01)
			MessageBox(0,L"Continue using and Generating Keys ,Thanks you are using Licensed machine",L"Cryptographic Analysis",MB_OK);
		else
		{
			MessageBox(0,L"You are violating rules of using this program in one machine",L"Cryptographic Analysis",MB_OK);
			exit(1);}
	   }---here end*/


		/* dwType = REG_SZ;
  dwSize = sizeof(m_szLastFileName);
  RegQueryValueEx(hKey, TEXT("LastFileName"), NULL, &dwType,
        (PBYTE)&m_szLastFileName, &dwSize);*/


/*			DWORD dwType, dwSize;
		   dwType = REG_DWORD;
		  dwSize = sizeof(DWORD);
		  RegSetValueEx(hKey, TEXT("MaxFileSize"), 0, dwType,
        (PBYTE)&m_dwMaxFileSize, dwSize);

		dwType = REG_SZ;
		dwSize = (_tcslen(m_szLastFileName) + 1) * sizeof(TCHAR);
		 RegSetValueEx(hKey, TEXT("LastFileName"), 0, dwType,
        (PBYTE)&m_szLastFileName, dwSize);*/

//            RegCloseKey(hKey);
  //     }

  //A_to_B(dest);

//	rnd_crv.form = 0;
//random_field( &curv->a6);
//	null( &rnd_crv.a2);

//	rnd_crv.a6.e[0]=0x0000001c;
//	rnd_crv.a6.e[1]=0x5e629417;
//	rnd_crv.a6.e[2]=0x3dbdf669;
//	rnd_crv.a6.e[3]=0xb9fca0fe;
//	rnd_crv.a6.e[4]=0xcd2165b0;

	print_curve("random curve", &rnd_crv);
	rand_POINT1(&Base, &rnd_crv);
//		Base.x.e[0x0]=0x0000001c;
//	Base.x.e[0x1]=0x5e629417;
//	Base.x.e[0x2]=0x3dbdf669;
//		Base.x.e[0x3]=0xb9fca0fe;
//		Base.x.e[0x4]=0xcd2165b0;

//		Base.y.e[0x0]=0x0000001a;
//		Base.y.e[0x1]=0xcb2e6c69;
//		Base.y.e[0x2]=0x258b70f8;
//		Base.y.e[0x3]=0xb9e7e01a;
//		Base.y.e[0x4]=0x010d0677;
	random_field(&private1);
	print_field("Side 1 secret:", &private1);
	random_field(&private2);
	print_field("Side 2 secret:", &private2);

//	printf("\nGenerate each sides public key\n\n");

	DH_gen_send_key( &Base, &rnd_crv, &private1, &P1);
	print_POINT1("Side 1 public key", &P1);
	DH_gen_send_key( &Base, &rnd_crv, &private2, &P2);
	print_POINT1("Side 2 public key", &P2);

//	printf("\nShow that each side gets the same shared secret\n\n");

	DH_key_share( &Base, &rnd_crv, &P2, &private1, &key1);
	print_field("encryption key 1 is", &key1);
//	printf("\n\n Please Enter a text of 19 charcters or less to encrypt ::: \n\n\t");

	DH_key_share( &Base, &rnd_crv, &P1, &private2, &key2);
	print_field("encryption key 1 is", &key2);

	//if (!ecckey) LString();

	/*   const jsize len = env->GetStringUTFLength(ecckey);
	   const char* strChars = env->GetStringUTFChars(ecckey, (jboolean *)0);

	   std::string Result(strChars, len);

	   env->ReleaseStringUTFChars(str, strChars);*/

memset(roro,0,10024);
memcpy(roro,data.c_str(),data.length());
//sprintf(pubfile,"%s",argv[3]);



std::string result=process_select_list();
std::string now=tohex(result);
//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring now : %s",now.c_str());

	//std:string	after=hex_to_string(now);

//	__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring after : %s",after.c_str());


printf("testing");

return env->NewStringUTF(now.c_str());
}

}


extern "C" {
jstring Java_future_im_UploadFile_EncryptKey(JNIEnv* env,jobject thiz,jstring ecckey)
		{
//	char* buf = (char *) new char[strlen(command_line)] ;
//	char *pTmp = buf;
//	strcpy(buf,command_line);
//char filename[300];



//    GetModuleFileName(NULL, filename, _MAX_PATH);
 //   argv[0] = filename;


	keysched KS;

//	int nFunsterStil;
//	HINSTANCE hThisInstance;
	int y=0;
	int t=0,x=0,z=0,v=0;
	unsigned char test[100];
	 char text[255];

	 char *dest;
//	random_seed = 0x129b25fe;

#ifdef TYPE2
	genlambda2();
#else
	genlambda();
#endif


std::string data=ConvertJString(env,ecckey);

//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring ecckey : %s",data.c_str());

//string buffer=_T"";
//LPTSTR omar=buffer.c_str();
 // CWin32InputBox::InputBox(L"hello", L"what?", omar, 100, true);
 // OutputDebugString(omar);
//LPTSTR szValue = new TCHAR[50];
// TCHAR *szValue=new TCHAR[50];
 // std::wstring koko=string_string(szValue);
  // if (sizeof(TCHAR)==1)
    //       string s=szValue;


//std:string string(szValue);
//  OutputDebugString(szValue);
/*	 char roro[100];
	strcpy(roro,data.c_str());
	std::stringstream ss;
	ss<<std::hex<<roro;
	ss>>w;
	random_seed=w;*/
time_t t1,t2;
//		srand(time(&t1));/* Create seed based on current time counted as seconds from 01/01/1970 */
//	srand(time(&t2));
	long randomnumber1,randomnumber2;

	randomnumber1 = rand();
	randomnumber2 = rand();
	random_seed = (randomnumber1 << 16) | randomnumber2;

	//random_seed=rand(); /* Generates different sequences at different runs */

	printf("Please to provide random seed with the encrypted file :0x%x\n",random_seed);
//	random_seed =0x129b25fe;
	random_seed=0x10f56281;
	unsigned long *random_digit;
	random_digit=&random_seed;

	Mother(random_digit);
	random_number=*random_digit;

	printf("create random curve and POINT1\n\n");

//	rand_curve(&rnd_crv);

	null( &rnd_crv.a2);

	rnd_crv.a6.e[0]=0x3F0DF748;
	rnd_crv.a6.e[1]=0x6D8008B2;
	rnd_crv.a6.e[2]=0x78E8BC91;
	rnd_crv.a6.e[3]=0x31B78B63;
	rnd_crv.a6.e[4]=0x3336671F;
	rnd_crv.a6.e[5]=0x1EA4A090;

//	3F0DF748,6D8008B2,78E8BC91,31B78B63,3336671F,1EA4A090



	memset(test,0,sizeof(test));
for (x=0;x<6;x++)
    for (z=3;z>=0;z--)
	{
			test[t]=(rnd_crv.a6.e[x]>>(z*8)&0xff);
			t++;
	}
test[t]='\0';

for (x=0;x<24;x++)
printf("%x ",test[x]);
//dest=test;
printf("\n");
for (x=0;x<24;x++)
cout <<" "<<hex<<setfill('0')<<setw(2)<<(int)test[x];

/*for (x=0;x<24;x++)
{
sprintf(text,"%02x", (int)test[x]);
string Omar=text;
inventory.append(" ");
inventory.append(Omar);
}*/

//std::string s;

//MessageBox(0,tito,L"Diffe Hellman Analysis",MB_OK);


//    HKEY hKey;
  //     DWORD dwDisp = 0;
   //    LPDWORD lpdwDisp = &dwDisp;
//	DWORD   m_dwMaxFileSize ; // 16k
//	TCHAR m_szLastFileName[50]={0};
//	 _tcscpy(m_szLastFileName, TEXT("Datafile.TXT"));

      // string l_strExampleKey = "SOFTWARE\\TestKey";

/*      LONG iSuccess = RegCreateKeyEx( HKEY_CURRENT_USER,TEXT("SOFTWARE\\TestKey"), 0L,NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hKey,lpdwDisp);

       if(iSuccess == ERROR_SUCCESS)
       {
		DWORD dwType, dwSize;
		   dwType = REG_DWORD;
  dwSize = sizeof(DWORD);
  RegQueryValueEx(hKey, TEXT("Key"), NULL, &dwType,
        (PBYTE)&m_dwMaxFileSize, &dwSize);
		if(m_dwMaxFileSize==0xabcdef01)
			MessageBox(0,L"Continue using and Generating Keys ,Thanks you are using Licensed machine",L"Cryptographic Analysis",MB_OK);
		else
		{
			MessageBox(0,L"You are violating rules of using this program in one machine",L"Cryptographic Analysis",MB_OK);
			exit(1);}
	   }---here end*/


		/* dwType = REG_SZ;
  dwSize = sizeof(m_szLastFileName);
  RegQueryValueEx(hKey, TEXT("LastFileName"), NULL, &dwType,
        (PBYTE)&m_szLastFileName, &dwSize);*/


/*			DWORD dwType, dwSize;
		   dwType = REG_DWORD;
		  dwSize = sizeof(DWORD);
		  RegSetValueEx(hKey, TEXT("MaxFileSize"), 0, dwType,
        (PBYTE)&m_dwMaxFileSize, dwSize);

		dwType = REG_SZ;
		dwSize = (_tcslen(m_szLastFileName) + 1) * sizeof(TCHAR);
		 RegSetValueEx(hKey, TEXT("LastFileName"), 0, dwType,
        (PBYTE)&m_szLastFileName, dwSize);*/

//            RegCloseKey(hKey);
  //     }

  //A_to_B(dest);

//	rnd_crv.form = 0;
//random_field( &curv->a6);
//	null( &rnd_crv.a2);

//	rnd_crv.a6.e[0]=0x0000001c;
//	rnd_crv.a6.e[1]=0x5e629417;
//	rnd_crv.a6.e[2]=0x3dbdf669;
//	rnd_crv.a6.e[3]=0xb9fca0fe;
//	rnd_crv.a6.e[4]=0xcd2165b0;

	print_curve("random curve", &rnd_crv);
	rand_POINT1(&Base, &rnd_crv);
//		Base.x.e[0x0]=0x0000001c;
//	Base.x.e[0x1]=0x5e629417;
//	Base.x.e[0x2]=0x3dbdf669;
//		Base.x.e[0x3]=0xb9fca0fe;
//		Base.x.e[0x4]=0xcd2165b0;

//		Base.y.e[0x0]=0x0000001a;
//		Base.y.e[0x1]=0xcb2e6c69;
//		Base.y.e[0x2]=0x258b70f8;
//		Base.y.e[0x3]=0xb9e7e01a;
//		Base.y.e[0x4]=0x010d0677;
	random_field(&private1);
	print_field("Side 1 secret:", &private1);
	random_field(&private2);
	print_field("Side 2 secret:", &private2);

//	printf("\nGenerate each sides public key\n\n");

	DH_gen_send_key( &Base, &rnd_crv, &private1, &P1);
	print_POINT1("Side 1 public key", &P1);
	DH_gen_send_key( &Base, &rnd_crv, &private2, &P2);
	print_POINT1("Side 2 public key", &P2);

//	printf("\nShow that each side gets the same shared secret\n\n");

	DH_key_share( &Base, &rnd_crv, &P2, &private1, &key1);
	print_field("encryption key 1 is", &key1);
//	printf("\n\n Please Enter a text of 19 charcters or less to encrypt ::: \n\n\t");

	DH_key_share( &Base, &rnd_crv, &P1, &private2, &key2);
	print_field("encryption key 1 is", &key2);

	//if (!ecckey) LString();

	/*   const jsize len = env->GetStringUTFLength(ecckey);
	   const char* strChars = env->GetStringUTFChars(ecckey, (jboolean *)0);

	   std::string Result(strChars, len);

	   env->ReleaseStringUTFChars(str, strChars);*/

memset(roro,0,10024);
memcpy(roro,data.c_str(),data.length());
//sprintf(pubfile,"%s",argv[3]);



std::string result=process_select_list();
std::string now=tohex(result);
//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring now : %s",now.c_str());

	//std:string	after=hex_to_string(now);

//	__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring after : %s",after.c_str());


printf("testing");

return env->NewStringUTF(now.c_str());
}

}



extern "C" {
jstring Java_future_im_GroupUploadFile_EncryptKey(JNIEnv* env,jobject thiz,jstring ecckey)
		{
//	char* buf = (char *) new char[strlen(command_line)] ;
//	char *pTmp = buf;
//	strcpy(buf,command_line);
//char filename[300];



//    GetModuleFileName(NULL, filename, _MAX_PATH);
 //   argv[0] = filename;


	keysched KS;

//	int nFunsterStil;
//	HINSTANCE hThisInstance;
	int y=0;
	int t=0,x=0,z=0,v=0;
	unsigned char test[100];
	 char text[255];

	 char *dest;
//	random_seed = 0x129b25fe;

#ifdef TYPE2
	genlambda2();
#else
	genlambda();
#endif


std::string data=ConvertJString(env,ecckey);

//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring ecckey : %s",data.c_str());

//string buffer=_T"";
//LPTSTR omar=buffer.c_str();
 // CWin32InputBox::InputBox(L"hello", L"what?", omar, 100, true);
 // OutputDebugString(omar);
//LPTSTR szValue = new TCHAR[50];
// TCHAR *szValue=new TCHAR[50];
 // std::wstring koko=string_string(szValue);
  // if (sizeof(TCHAR)==1)
    //       string s=szValue;


//std:string string(szValue);
//  OutputDebugString(szValue);
/*	 char roro[100];
	strcpy(roro,data.c_str());
	std::stringstream ss;
	ss<<std::hex<<roro;
	ss>>w;
	random_seed=w;*/
time_t t1,t2;
//		srand(time(&t1));/* Create seed based on current time counted as seconds from 01/01/1970 */
//	srand(time(&t2));
	long randomnumber1,randomnumber2;

	randomnumber1 = rand();
	randomnumber2 = rand();
	random_seed = (randomnumber1 << 16) | randomnumber2;

	//random_seed=rand(); /* Generates different sequences at different runs */

	printf("Please to provide random seed with the encrypted file :0x%x\n",random_seed);
//	random_seed =0x129b25fe;
	random_seed=0x10f56281;
	unsigned long *random_digit;
	random_digit=&random_seed;

	Mother(random_digit);
	random_number=*random_digit;

	printf("create random curve and POINT1\n\n");

//	rand_curve(&rnd_crv);

	null( &rnd_crv.a2);

	rnd_crv.a6.e[0]=0x3F0DF748;
	rnd_crv.a6.e[1]=0x6D8008B2;
	rnd_crv.a6.e[2]=0x78E8BC91;
	rnd_crv.a6.e[3]=0x31B78B63;
	rnd_crv.a6.e[4]=0x3336671F;
	rnd_crv.a6.e[5]=0x1EA4A090;

//	3F0DF748,6D8008B2,78E8BC91,31B78B63,3336671F,1EA4A090



	memset(test,0,sizeof(test));
for (x=0;x<6;x++)
    for (z=3;z>=0;z--)
	{
			test[t]=(rnd_crv.a6.e[x]>>(z*8)&0xff);
			t++;
	}
test[t]='\0';

for (x=0;x<24;x++)
printf("%x ",test[x]);
//dest=test;
printf("\n");
for (x=0;x<24;x++)
cout <<" "<<hex<<setfill('0')<<setw(2)<<(int)test[x];

/*for (x=0;x<24;x++)
{
sprintf(text,"%02x", (int)test[x]);
string Omar=text;
inventory.append(" ");
inventory.append(Omar);
}*/

//std::string s;

//MessageBox(0,tito,L"Diffe Hellman Analysis",MB_OK);


//    HKEY hKey;
  //     DWORD dwDisp = 0;
   //    LPDWORD lpdwDisp = &dwDisp;
//	DWORD   m_dwMaxFileSize ; // 16k
//	TCHAR m_szLastFileName[50]={0};
//	 _tcscpy(m_szLastFileName, TEXT("Datafile.TXT"));

      // string l_strExampleKey = "SOFTWARE\\TestKey";

/*      LONG iSuccess = RegCreateKeyEx( HKEY_CURRENT_USER,TEXT("SOFTWARE\\TestKey"), 0L,NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hKey,lpdwDisp);

       if(iSuccess == ERROR_SUCCESS)
       {
		DWORD dwType, dwSize;
		   dwType = REG_DWORD;
  dwSize = sizeof(DWORD);
  RegQueryValueEx(hKey, TEXT("Key"), NULL, &dwType,
        (PBYTE)&m_dwMaxFileSize, &dwSize);
		if(m_dwMaxFileSize==0xabcdef01)
			MessageBox(0,L"Continue using and Generating Keys ,Thanks you are using Licensed machine",L"Cryptographic Analysis",MB_OK);
		else
		{
			MessageBox(0,L"You are violating rules of using this program in one machine",L"Cryptographic Analysis",MB_OK);
			exit(1);}
	   }---here end*/


		/* dwType = REG_SZ;
  dwSize = sizeof(m_szLastFileName);
  RegQueryValueEx(hKey, TEXT("LastFileName"), NULL, &dwType,
        (PBYTE)&m_szLastFileName, &dwSize);*/


/*			DWORD dwType, dwSize;
		   dwType = REG_DWORD;
		  dwSize = sizeof(DWORD);
		  RegSetValueEx(hKey, TEXT("MaxFileSize"), 0, dwType,
        (PBYTE)&m_dwMaxFileSize, dwSize);

		dwType = REG_SZ;
		dwSize = (_tcslen(m_szLastFileName) + 1) * sizeof(TCHAR);
		 RegSetValueEx(hKey, TEXT("LastFileName"), 0, dwType,
        (PBYTE)&m_szLastFileName, dwSize);*/

//            RegCloseKey(hKey);
  //     }

  //A_to_B(dest);

//	rnd_crv.form = 0;
//random_field( &curv->a6);
//	null( &rnd_crv.a2);

//	rnd_crv.a6.e[0]=0x0000001c;
//	rnd_crv.a6.e[1]=0x5e629417;
//	rnd_crv.a6.e[2]=0x3dbdf669;
//	rnd_crv.a6.e[3]=0xb9fca0fe;
//	rnd_crv.a6.e[4]=0xcd2165b0;

	print_curve("random curve", &rnd_crv);
	rand_POINT1(&Base, &rnd_crv);
//		Base.x.e[0x0]=0x0000001c;
//	Base.x.e[0x1]=0x5e629417;
//	Base.x.e[0x2]=0x3dbdf669;
//		Base.x.e[0x3]=0xb9fca0fe;
//		Base.x.e[0x4]=0xcd2165b0;

//		Base.y.e[0x0]=0x0000001a;
//		Base.y.e[0x1]=0xcb2e6c69;
//		Base.y.e[0x2]=0x258b70f8;
//		Base.y.e[0x3]=0xb9e7e01a;
//		Base.y.e[0x4]=0x010d0677;
	random_field(&private1);
	print_field("Side 1 secret:", &private1);
	random_field(&private2);
	print_field("Side 2 secret:", &private2);

//	printf("\nGenerate each sides public key\n\n");

	DH_gen_send_key( &Base, &rnd_crv, &private1, &P1);
	print_POINT1("Side 1 public key", &P1);
	DH_gen_send_key( &Base, &rnd_crv, &private2, &P2);
	print_POINT1("Side 2 public key", &P2);

//	printf("\nShow that each side gets the same shared secret\n\n");

	DH_key_share( &Base, &rnd_crv, &P2, &private1, &key1);
	print_field("encryption key 1 is", &key1);
//	printf("\n\n Please Enter a text of 19 charcters or less to encrypt ::: \n\n\t");

	DH_key_share( &Base, &rnd_crv, &P1, &private2, &key2);
	print_field("encryption key 1 is", &key2);

	//if (!ecckey) LString();

	/*   const jsize len = env->GetStringUTFLength(ecckey);
	   const char* strChars = env->GetStringUTFChars(ecckey, (jboolean *)0);

	   std::string Result(strChars, len);

	   env->ReleaseStringUTFChars(str, strChars);*/

memset(roro,0,10024);
memcpy(roro,data.c_str(),data.length());
//sprintf(pubfile,"%s",argv[3]);



std::string result=process_select_list();
std::string now=tohex(result);
//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring now : %s",now.c_str());

	//std:string	after=hex_to_string(now);

//	__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring after : %s",after.c_str());


printf("testing");

return env->NewStringUTF(now.c_str());
}

}


extern "C" {
jbyteArray Java_future_im_UploadFile_EncryptBinfile(JNIEnv* env,jobject thiz,jbyteArray message)
		{
//	char* buf = (char *) new char[strlen(command_line)] ;
//	char *pTmp = buf;
//	strcpy(buf,command_line);
//char filename[300];



//    GetModuleFileName(NULL, filename, _MAX_PATH);
 //   argv[0] = filename;


	keysched KS;

//	int nFunsterStil;
//	HINSTANCE hThisInstance;
	int y=0;
	int t=0,x=0,z=0,v=0;
	unsigned char test[100];
	 char text[255];

	 char *dest;
//	random_seed = 0x129b25fe;

#ifdef TYPE2
	genlambda2();
#else
	genlambda();
#endif


//std::string data=ConvertJString(env,ecckey);
	jsize arrayLen = env->GetArrayLength(message);
	jbyte* data = env->GetByteArrayElements(message, NULL);
//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring ecckey : %s",data.c_str());

//string buffer=_T"";
//LPTSTR omar=buffer.c_str();
 // CWin32InputBox::InputBox(L"hello", L"what?", omar, 100, true);
 // OutputDebugString(omar);
//LPTSTR szValue = new TCHAR[50];
// TCHAR *szValue=new TCHAR[50];
 // std::wstring koko=string_string(szValue);
  // if (sizeof(TCHAR)==1)
    //       string s=szValue;


//std:string string(szValue);
//  OutputDebugString(szValue);
/*	 char roro[100];
	strcpy(roro,data.c_str());
	std::stringstream ss;
	ss<<std::hex<<roro;
	ss>>w;
	random_seed=w;*/
time_t t1,t2;
//		srand(time(&t1));/* Create seed based on current time counted as seconds from 01/01/1970 */
//	srand(time(&t2));
	long randomnumber1,randomnumber2;

	randomnumber1 = rand();
	randomnumber2 = rand();
	random_seed = (randomnumber1 << 16) | randomnumber2;

	//random_seed=rand(); /* Generates different sequences at different runs */

	printf("Please to provide random seed with the encrypted file :0x%x\n",random_seed);
//	random_seed =0x129b25fe;
	random_seed=0x10f56281;
	unsigned long *random_digit;
	random_digit=&random_seed;

	Mother(random_digit);
	random_number=*random_digit;

	printf("create random curve and POINT1\n\n");

//	rand_curve(&rnd_crv);

	null( &rnd_crv.a2);

	rnd_crv.a6.e[0]=0x3F0DF748;
	rnd_crv.a6.e[1]=0x6D8008B2;
	rnd_crv.a6.e[2]=0x78E8BC91;
	rnd_crv.a6.e[3]=0x31B78B63;
	rnd_crv.a6.e[4]=0x3336671F;
	rnd_crv.a6.e[5]=0x1EA4A090;

//	3F0DF748,6D8008B2,78E8BC91,31B78B63,3336671F,1EA4A090



	memset(test,0,sizeof(test));
for (x=0;x<6;x++)
    for (z=3;z>=0;z--)
	{
			test[t]=(rnd_crv.a6.e[x]>>(z*8)&0xff);
			t++;
	}
test[t]='\0';

for (x=0;x<24;x++)
printf("%x ",test[x]);
//dest=test;
printf("\n");
for (x=0;x<24;x++)
cout <<" "<<hex<<setfill('0')<<setw(2)<<(int)test[x];

/*for (x=0;x<24;x++)
{
sprintf(text,"%02x", (int)test[x]);
string Omar=text;
inventory.append(" ");
inventory.append(Omar);
}*/

//std::string s;

//MessageBox(0,tito,L"Diffe Hellman Analysis",MB_OK);


//    HKEY hKey;
  //     DWORD dwDisp = 0;
   //    LPDWORD lpdwDisp = &dwDisp;
//	DWORD   m_dwMaxFileSize ; // 16k
//	TCHAR m_szLastFileName[50]={0};
//	 _tcscpy(m_szLastFileName, TEXT("Datafile.TXT"));

      // string l_strExampleKey = "SOFTWARE\\TestKey";

/*      LONG iSuccess = RegCreateKeyEx( HKEY_CURRENT_USER,TEXT("SOFTWARE\\TestKey"), 0L,NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hKey,lpdwDisp);

       if(iSuccess == ERROR_SUCCESS)
       {
		DWORD dwType, dwSize;
		   dwType = REG_DWORD;
  dwSize = sizeof(DWORD);
  RegQueryValueEx(hKey, TEXT("Key"), NULL, &dwType,
        (PBYTE)&m_dwMaxFileSize, &dwSize);
		if(m_dwMaxFileSize==0xabcdef01)
			MessageBox(0,L"Continue using and Generating Keys ,Thanks you are using Licensed machine",L"Cryptographic Analysis",MB_OK);
		else
		{
			MessageBox(0,L"You are violating rules of using this program in one machine",L"Cryptographic Analysis",MB_OK);
			exit(1);}
	   }---here end*/


		/* dwType = REG_SZ;
  dwSize = sizeof(m_szLastFileName);
  RegQueryValueEx(hKey, TEXT("LastFileName"), NULL, &dwType,
        (PBYTE)&m_szLastFileName, &dwSize);*/


/*			DWORD dwType, dwSize;
		   dwType = REG_DWORD;
		  dwSize = sizeof(DWORD);
		  RegSetValueEx(hKey, TEXT("MaxFileSize"), 0, dwType,
        (PBYTE)&m_dwMaxFileSize, dwSize);

		dwType = REG_SZ;
		dwSize = (_tcslen(m_szLastFileName) + 1) * sizeof(TCHAR);
		 RegSetValueEx(hKey, TEXT("LastFileName"), 0, dwType,
        (PBYTE)&m_szLastFileName, dwSize);*/

//            RegCloseKey(hKey);
  //     }

  //A_to_B(dest);

//	rnd_crv.form = 0;
//random_field( &curv->a6);
//	null( &rnd_crv.a2);

//	rnd_crv.a6.e[0]=0x0000001c;
//	rnd_crv.a6.e[1]=0x5e629417;
//	rnd_crv.a6.e[2]=0x3dbdf669;
//	rnd_crv.a6.e[3]=0xb9fca0fe;
//	rnd_crv.a6.e[4]=0xcd2165b0;

	print_curve("random curve", &rnd_crv);
	rand_POINT1(&Base, &rnd_crv);
//		Base.x.e[0x0]=0x0000001c;
//	Base.x.e[0x1]=0x5e629417;
//	Base.x.e[0x2]=0x3dbdf669;
//		Base.x.e[0x3]=0xb9fca0fe;
//		Base.x.e[0x4]=0xcd2165b0;

//		Base.y.e[0x0]=0x0000001a;
//		Base.y.e[0x1]=0xcb2e6c69;
//		Base.y.e[0x2]=0x258b70f8;
//		Base.y.e[0x3]=0xb9e7e01a;
//		Base.y.e[0x4]=0x010d0677;
	random_field(&private1);
	print_field("Side 1 secret:", &private1);
	random_field(&private2);
	print_field("Side 2 secret:", &private2);

//	printf("\nGenerate each sides public key\n\n");

	DH_gen_send_key( &Base, &rnd_crv, &private1, &P1);
	print_POINT1("Side 1 public key", &P1);
	DH_gen_send_key( &Base, &rnd_crv, &private2, &P2);
	print_POINT1("Side 2 public key", &P2);

//	printf("\nShow that each side gets the same shared secret\n\n");

	DH_key_share( &Base, &rnd_crv, &P2, &private1, &key1);
	print_field("encryption key 1 is", &key1);
//	printf("\n\n Please Enter a text of 19 charcters or less to encrypt ::: \n\n\t");

	DH_key_share( &Base, &rnd_crv, &P1, &private2, &key2);
	print_field("encryption key 1 is", &key2);

	//if (!ecckey) LString();

	/*   const jsize len = env->GetStringUTFLength(ecckey);
	   const char* strChars = env->GetStringUTFChars(ecckey, (jboolean *)0);

	   std::string Result(strChars, len);

	   env->ReleaseStringUTFChars(str, strChars);*/

memset(roro,0,10024);
memcpy(roro,data,arrayLen);
//sprintf(pubfile,"%s",argv[3]);



std::string result=process_select_list();
std::string now=tohex(result);
//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring now : %s",now.c_str());

	//std:string	after=hex_to_string(now);

//	__android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "mystring after : %s",after.c_str());


printf("testing");

jbyteArray ret;

ret = env->NewByteArray(now.length());


(env)->SetByteArrayRegion(ret,0,now.length(), (const jbyte*)now.c_str());

 return ret;
}

}


/*jbyteArray convertPointerToByteArray (const void* pointer) {
 jbyteArray arr = (*globalenv)->NewByteArray(globalenv, sizeof(void*));
 (*globalenv)->SetByteArrayRegion(globalenv, arr,0,sizeof(void*),(jbyte*)&pointer);
 return arr;
}*/

std::string process_select_list()
{
std:string encrypteddata;
	int i, null_ok, precision, scale;
    chunk keydes1,keydes2,keydes3, data1, data2,data3,decdata1,decdata2,decdata3;
    keysched KS;
    char buffer[1024];
    FILE *debugFile;
    
//	char *start;
	char *leftstart;
//	char *end;
    char *end;
    char *start;
    char array0[10024];
	
	strcpy(array0,roro);
	char *array1=array0;
	char *temp3=array0;
	char tempe1[10024];
	char *temp1=tempe1;
	char tempe2[10024];
	char *temp2=tempe2;
	

    char num[15];
    char numd[15];
    char sondos[1024];
    int t=0;
    int soso=0;
    //INDEX i;
	int x=0;
	int z=1;
	int u=0;
    int	n=0;
	int number,len1,left,y,found,firstkey;
firstkey=0;

sondos[0]='\0';
//found=1;



//	buffer = (char*) malloc (sizeof(char)*4);
	memset(buffer,0,4);
  if (buffer == NULL) {fputs ("Memory error",stderr); exit (2);}

//  memset(infile,0,100);
//  memset(outfile,0,100);
 
 
 // strcat(infile,"PUBLICKEY");

//pFile = fopen (infile,"rb" );
//  if (pFile==null) {fputs ("File error",stderr); exit (1);}
	/*write the public key*/
/*for (x=0;x<6;x++)  
    for (z=3;z>=0;z--)
	{		P1x[0]=(P2.x.e[x]>>(z*8)&0xff);
	}
for (x=0;x<6;x++)  
    for (z=3;z>=0;z--)
	{		P1y[0]=(P2.y.e[x]>>(z*8)&0xff);
	}*/
/*write the seed*/
//	random_number^=0x10f56281;
	unsigned char data11[5];
	unsigned char data22[5];
	unsigned char data33[5];
	unsigned char seed[1];
	std::string appended="";
	 unsigned char text[100];
	memset(text,0,100);
	for (z=3;z>=0;z--)
	{		seed[0]=(random_number>>(z*8)&0xff);
	   stringstream s;
      
		s<<seed[0];
         
         string X = s.str();
	encrypteddata.append(X);
	//		fwrite(seed,1,1,pFile);
	}
	
	 unsigned char P1x[1];
	 unsigned char P1y[1];
	P1x[0]='\0';
	P1y[0]='\0';

	for (x=0;x<6;x++)  
    for (z=3;z>=0;z--)
	{		
		
		P1x[0]=(P2.x.e[x]>>(z*8)&0xff);
	stringstream z;
      
  
 
         z << P1x[0];
 
         string Y = z.str();
	//string Y=text;
	encrypteddata.append(Y);
	}
for (x=0;x<6;x++)  
    for (z=3;z>=0;z--)
	{		P1y[0]=(P2.y.e[x]>>(z*8)&0xff);
	   stringstream s;
      
 
         s << P1y[0];
 
         string Z = s.str();
			encrypteddata.append(Z);

	}


	
        soso=strlen(array1);
        array1[soso]=0x7F;
        array1[soso+1]='\0';
       // printf ("\n");


        		start=array1;
	  len1=strlen(array1);
	  number=len1/24;
	 left=len1%24;
	 y=number*24;
	leftstart=array1+y;
	 end =array1+y+left;
	
/*prepare the keys if first time */
//call the function fillkeys();

if (n==0)
{
for (x=0;x<2;x++)  
    for (z=3;z>=0;z--)
	{		keydes1.b[t]=(key1.e[x]>>(z*8)&0xff);
			t++;
	}
t=0;
for (x=2;x<4;x++)  
    for (z=3;z>=0;z--)
	{		keydes2.b[t]=(key1.e[x]>>(z*8)&0xff);
			t++;
	}
t=0;
for (x=4;x<6;x++)  
    for (z=3;z>=0;z--)
	{		keydes3.b[t]=(key1.e[x]>>(z*8)&0xff);
			t++;
	}
t=0;
}

	 for (int g=1;g<number+1;g++)
	 {
		 if (firstkey==0)
		 {
			 
		   //read 8 bytes from the first block
		//	 fencrypt(); with k1
			 fsetkey((char *)&keydes1, (keysched *)KS);
			for (i=0;i<8;i++)
			{
				data1.b[t]=*array1++;
				t++;
			}
			fencrypt((char *)&data1, 0, (keysched *)KS);
			for (t=0;t<8;t++)
			{
					data11[0]=data1.b[t];
					 stringstream s;
      
 
         s << data11[0];
 
         string Z = s.str();
			encrypteddata.append(Z);
//					printf("%x",data11[0]);
				
//			
			}
			
//				fencrypt(&data1,1,KS);
//				printf("\n");
//			for (t=0;t<8;t++)
//			{
//			printf("%c",data1.b[t]);
//			data111.arr[0]=data1.b[t];
//EXEC SQL CALL dbms_output.put_line(:data111);
//			}
//			printf("\n");

firstkey++;
t=0;
		 }	

		if (firstkey==1)
		 {
			 fsetkey((char *)&keydes2, (keysched *)KS);
			for (i=8;i<16;i++)
			{
				data2.b[t]=*array1++;
				t++;
			}
			fencrypt((char *)&data2, 0, (keysched *)KS);	
		for (t=0;t<8;t++)
			{
			data22[0]=data2.b[t];
//			
							 stringstream s;
      
 
         s << data22[0];
 
         string Z = s.str();
			encrypteddata.append(Z);	
//			
			}
			
//						fencrypt(&data2,1,KS);
//				printf("\n");
//			for (t=0;t<8;t++)
//			{
////			printf("%c",data2.b[t]);
//				data222.arr[0]=data2.b[t];
//EXEC SQL CALL dbms_output.put_line(:data222);
//			}
//			printf("\n");
						
						
						firstkey++;
t=0;
		 }	

		if (firstkey==2)
		 {
		 fsetkey((char *)&keydes3,(keysched *) KS);
			for (i=16;i<24;i++)
			{
				data3.b[t]=*array1++;
				t++;
			}
			fencrypt((char *)&data3, 0, (keysched *)KS);
		for (t=0;t<8;t++)
			{
			data33[0]=data3.b[t];

							 stringstream s;
      
 
         s << data33[0];
 
         string Z = s.str();
			encrypteddata.append(Z);

//				printf("%x",data33[0]);
				
//			
			}
			
			
//						fencrypt(&data3,1,KS);
//					printf("\n");
//						for (t=0;t<8;t++)
//			{
//			printf("%c",data3.b[t]);
//			data333.arr[0]=data3.b[t];
//EXEC SQL CALL dbms_output.put_line(:data333);
//			}
//						printf("\n");
						
						
						
						firstkey++;
		t=0;
		 }	
firstkey=0;
	 }


temp1[0]='\0';
strncpy(temp1,leftstart,left);
*(temp1+left)='\0';
//*array1='\0';
for(i=0;i<number;i++) /*restart from begining to avoid memory fault*/
	for(u=0;u<24;u++)
		*array1--;
*array1='\0';

i=0;
u=0;
strcpy(array1,temp1);
temp2=array1;
array1=array1+left;

n++;
     
        
        
        
 
if (left!=0)
 {
 if (firstkey==0)
	 if(left>0)
		 {
			 
		   //read 8 bytes from the first block
		//	 fencrypt(); with k1
			 fsetkey((char *)&keydes1, (keysched *)KS);
			for (i=0;i<8;i++)
			{
				data1.b[t]=*temp1++;
				t++;
			}
			fencrypt((char *)&data1, 0, (keysched *)KS);
			for (t=0;t<8;t++)
			{
					data11[0]=data1.b[t];
								 stringstream s;
      
 
         s << data11[0];
 
         string Z = s.str();
			encrypteddata.append(Z);

					
					
					//					printf("\n left values %x",data11[0]);
				
//			
			}
		//		fencrypt((char *)&data1,1,(keysched *)KS);
//				printf("\n");
//		 for (t=0;t<8;t++)
//			{
//			printf("%c",data1.b[t]);
//			data111.arr[0]=data1.b[t];
//EXEC SQL CALL dbms_output.put_line(:data111);
//			}
//				printf("\n");
				
				
				firstkey++;
t=0;
		 }	

		if (firstkey==1)
			if(left>8)
		 {
			 fsetkey((char *)&keydes2, (keysched *)KS);
			for (i=8;i<16;i++)
			{
				data2.b[t]=*temp1++;
				t++;
			}
			fencrypt((char *)&data2, 0, (keysched *)KS);	
		for (t=0;t<8;t++)
			{
			data22[0]=data2.b[t];
					 stringstream s;
      
 
         s << data22[0];
 
         string Z = s.str();
			encrypteddata.append(Z);

			
			
			
			//			printf("\n left values %x",data22[0]);
				//			
			}
			
//						fencrypt(&data2,1,KS);
//						printf("\n");
//						for (t=0;t<8;t++)
//			{
//			printf("%c",data2.b[t]);
//			data222.arr[0]=data2.b[t];
//EXEC SQL CALL dbms_output.put_line(:data222);
//			}
//						printf("\n");
firstkey++;
t=0;
		 }	

		if (firstkey==2)
			if(left>16)
		 {
		 fsetkey((char *)&keydes3, (keysched *)KS);
			for (i=16;i<24;i++)
			{
				data3.b[t]=*temp1++;
				t++;
			}
			fencrypt((char *)&data3, 0, (keysched *)KS);
		for (t=0;t<8;t++)
			{
			data33[0]=data3.b[t];
								 stringstream s;
      
 
         s << data33[0];
 
         string Z = s.str();
			encrypteddata.append(Z);

//			printf("\n left values %x",data33[0]);
				
//			
			}
			
			
//						fencrypt(&data3,1,KS);
//						printf("\n");
//		for (t=0;t<8;t++)
//			{
//			printf("%c",data3.b[t]);
//			data333.arr[0]=data3.b[t];
//EXEC SQL CALL dbms_output.put_line(:data333);
//			}
//						printf("\n");
			firstkey++;
		t=0;
		 }	
firstkey=0;
   
}
    

    

	//exit(1);
    return encrypteddata;
}




void copy (FIELD2N *a,FIELD2N *b)

{
        INDEX i;

        SUMLOOP(i)  b->e[i] = a->e[i];
}






void null(FIELD2N *a)

{
        INDEX i;

        SUMLOOP(i)  a->e[i] = 0;
}

void genlambda2()
{
	INDEX	i, logof[4], n, index, j, k;
	INDEX	log2[field_prime], twoexp;

/*  build log table first.  For the case where 2 generates the quadradic
	residues instead of the field, duplicate all the entries to ensure 
	positive and negative matches in the lookup table (that is, -k mod
	field_prime is congruent to entry field_prime + k).  */

	twoexp = 1;
	for (i=0; i<NUMBITS; i++)
	{
		log2[twoexp] = i;
		twoexp = (twoexp << 1) % field_prime;
	}
	if (twoexp == 1)		/*  if so, then deal with quadradic residues */
	{
		twoexp = 2*NUMBITS;
		for (i=0; i<NUMBITS; i++)
		{
			log2[twoexp] = i;
			twoexp = (twoexp << 1) % field_prime;
		}
	}
	else
	{
		for (i=NUMBITS; i<field_prime-1; i++)
		{
			log2[twoexp] = i;
			twoexp = (twoexp << 1) % field_prime;
		}
	}
		
/*  first element in vector 1 always = 1  */

	lambdaa[0][0] = 1;
	lambdaa[1][0] = -1;

/*  again compute n = (field_prime - 1)/2 but this time we use it to see if
	an equation applies  */
	
	n = (field_prime - 1)/2;

/*  as in genlambda for Type I we can loop over 2^index and look up index 
	from the log table previously built.  But we have to work with 4 
	equations instead of one and only two of those are useful.  Look up 
	all four solutions and put them into an array.  Use two counters, one
	called j to step thru the 4 solutions and the other called k to track
	the two valid ones.
	
	For the case when 2 generates quadradic residues only 2 equations are
	really needed.  But the same math works due to the way we filled the
	log2 table.
*/

	twoexp = 1;	
	for (i=1; i<n; i++)
	{
		twoexp = (twoexp<<1) % field_prime;
		logof[0] = log2[field_prime + 1 - twoexp];
		logof[1] = log2[field_prime - 1 - twoexp];
		logof[2] = log2[twoexp - 1];
		logof[3] = log2[twoexp + 1];
		k = 0;
		j = 0;
		while (k<2)
		{
			if (logof[j] < n)
			{
				lambdaa[k][i] = logof[j];
				k++;
			}
			j++;
		}
	}

/*  find most significant bit of NUMBITS.  This is int(log_2(NUMBITS)).  
	Used in opt_inv to count number of bits.  */

	lg2_mm = log_2((ELEMENT)(NUMBITS - 1));
}


void genlambda()
{
        INDEX i, logof, n, index;
        INDEX log2[field_prime], twoexp;

        for (i=0; i<field_prime; i++) log2[i] = -1;

/*  build antilog table first  */

        twoexp = 1;
        for (i=0; i<field_prime; i++) 
        {
          log2[twoexp] = i;
          twoexp = (twoexp << 1) % field_prime;
        }

/*  compute n for easy reference */

        n = (field_prime - 1)/2;
        
/*  fill in first vector with indicies shifted by half table size  */

        lambdaa[0][0] = n;
        for (i=1; i<field_prime; i++) 
        	lambdaa[0][i] = (lambdaa[0][i-1] + 1) % NUMBITS;

/*  initialize second vector with known values  */
        
        lambdaa[1][0]= -1;		/*  never used  */
        lambdaa[1][1] = n;
        lambdaa[1][n] = 1;

/*  loop over result space.  Since we want 2^i + 2^j = 1 mod field_prime
        it's a ton easier to loop on 2^i and look up i then solve the silly
        equations.  Think about it, make a table, and it'll be obvious.  */

        for (i=2; i<=n; i++) {
          index = log2[i];
          logof = log2[field_prime - i + 1];
          lambdaa[1][index] = logof;
          lambdaa[1][logof] = index;
        }
/*  last term, it's the only one which equals itself.  See references.  */

        lambdaa[1][log2[n+1]] = log2[n+1];

/*  find most significant bit of NUMBITS.  This is int(log_2(NUMBITS)).  
	Used in opt_inv to count number of bits.  */

	lg2_mm = log_2((ELEMENT)(NUMBITS - 1));
	
}

INDEX log_2( ELEMENT x)

{
	INDEX	k, lg2;
	ELEMENT ebit, bitsave, bitmask;

	lg2 = 0;
	bitsave = x;				/* grab bits we're interested in.  */
	k = WORDSIZE/2;					/* first see if msb is in top half  */
	bitmask = -1L<<k;				/* of all bits  */
	while (k)
	{
		ebit = bitsave & bitmask;	/* did we hit a bit?  */
		if (ebit)					/* yes  */
		{
			lg2 += k;				/* increment degree by minimum possible offset  */
			bitsave = ebit;			/* and zero out non useful bits  */
		}
		k /= 2;
		bitmask ^= (bitmask >> k);
	}
	return( lg2);
}

/*std::wstring s2ws(const std::string& s)
{
 int len;
 int slength = (int)s.length() + 1;
 len = MultiByteToWideChar(CP_ACP, 0, s.c_str(), slength, 0, 0); 
 wchar_t* buf = new wchar_t[len];
 MultiByteToWideChar(CP_ACP, 0, s.c_str(), slength, buf, len);
 std::wstring r(buf);
 delete[] buf;
 return r;
}*/






