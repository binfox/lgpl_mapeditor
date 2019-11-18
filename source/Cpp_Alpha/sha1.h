/*
 *  sha1.h
 *
 *  Description:
 *      This is the header file for code which implements the Secure
 *      Hashing Algorithm 1 as defined in FIPS PUB 180-1 published
 *      April 17, 1995.
 *
 *      Many of the variable names in this code, especially the
 *      single character names, were used because those were the names
 *      used in the publication.
 *
 *      Please read the file sha1.c for more information.
 *
 */

#ifndef _SHA1_H_
#define _SHA1_H_


namespace sha1
{
	enum
	{
		shaSuccess = 0,
		shaNull,            /* Null pointer parameter */
		shaInputTooLong,    /* input data too long */
		shaStateError       /* called Input after Result */
	};


	const int SHA1HashSize = 20;


	/*
	 *  This structure will hold context information for the SHA-1
	 *  hashing operation
	 */
	typedef struct _SHA1Context
	{
		unsigned long	Intermediate_Hash[SHA1HashSize/4];	/* Message Digest  */

		unsigned long	Length_Low;				/* Message length in bits      */
		unsigned long	Length_High;			/* Message length in bits      */

		short			Message_Block_Index;	/* Index into message block array   */
		unsigned char	Message_Block[64];		/* 512-bit message blocks      */

		int				Computed;				/* Is the digest computed?         */
		int				Corrupted;				/* Is the message digest corrupted? */
	} SHA1Context;


	/*
	 *  Function Prototypes
	 */
	int SHA1Reset(sha1::SHA1Context*);
	int SHA1Input(sha1::SHA1Context*, const char*, unsigned int);
	int SHA1Result(sha1::SHA1Context*, unsigned char Message_Digest[sha1::SHA1HashSize]);
}


#endif // _SHA1_H_