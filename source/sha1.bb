Global sha1_low
Global sha1_high
Global sha1_index
Global sha1_hash[4]
Global sha1_message[63]





;---------------------------------------------------------------------
;txt:    text string
;RETURN: sha1-fingerprint (20 byte)
;---------------------------------------------------------------------
Function sha1$(txt$)
  Local ascii
  Local i
  Local length

  sha1_reset()
  length=Len(txt$)

  For i=1 To length
    ascii=Asc(Mid$(txt$,i,1))
    sha1_message[sha1_index]=ascii
    sha1_index=sha1_index+1

    sha1_low=sha1_low+8
    If sha1_low=0 Then
      sha1_high=sha1_high+1
      If sha1_high=0 Then Return
    EndIf

    If sha1_index=64 Then sha1_process()
  Next

  Return sha1_result$()
End Function





;---------------------------------------------------------------------
Function sha1_pad()
  If sha1_index>55 Then

    sha1_message[sha1_index]=$80
    sha1_index=sha1_index+1

    While sha1_index<64
      sha1_message[sha1_index]=0
      sha1_index=sha1_index+1
    Wend

    sha1_process()

    While sha1_index<56
      sha1_message[sha1_index]=0
      sha1_index=sha1_index+1
    Wend

  Else

    sha1_message[sha1_index]=$80
    sha1_index=sha1_index+1

    While sha1_index<56
      sha1_message[sha1_index]=0
      sha1_index=sha1_index+1
    Wend

  EndIf

  sha1_message[56]=sha1_high Shr 24
  sha1_message[57]=sha1_high Shr 16
  sha1_message[58]=sha1_high Shr 8
  sha1_message[59]=sha1_high
  sha1_message[60]=sha1_low Shr 24
  sha1_message[61]=sha1_low Shr 16
  sha1_message[62]=sha1_low Shr 8
  sha1_message[63]=sha1_low

  sha1_process()
End Function





;---------------------------------------------------------------------
Function sha1_process()
  Local a
  Local b
  Local c
  Local d
  Local e
  Local k[3]
  Local t
  Local temp
  Local w[79]

  k[0]=$5A827999
  k[1]=$6ED9EBA1
  k[2]=$8F1BBCDC
  k[3]=$CA62C1D6

  For t=0 To 15
    w[t]=sha1_message[t*4] Shl 24
    w[t]=w[t] Or sha1_message[t*4+1] Shl 16
    w[t]=w[t] Or sha1_message[t*4+2] Shl 8
    w[t]=w[t] Or sha1_message[t*4+3]
  Next

  For t=16 To 79
    w[t]=sha1_rotateleft(w[t-3] Xor w[t-8] Xor w[t-14] Xor w[t-16],1)
  Next

  a=sha1_hash[0]
  b=sha1_hash[1]
  c=sha1_hash[2]
  d=sha1_hash[3]
  e=sha1_hash[4]

  For t=0 To 19
    temp=sha1_rotateleft(A,5) + ((B And C) Or ((~B) And D)) + E + W[t] + K[0]
    e=d
    d=c
    c=sha1_rotateleft(B,30)
    b=a
    a=temp
  Next

  For t=20 To 39
    temp=sha1_rotateleft(A,5) + (B Xor C Xor D) + E + W[t] + K[1]
    e=d
    d=c
    c=sha1_rotateleft(B,30)
    b=a
    a=temp
  Next

  For t=40 To 59
    temp=sha1_rotateleft(A,5) + ((B And C) Or (B And D) Or (C And D)) + E + W[t] + K[2]
    e=d
    d=c
    c=sha1_rotateleft(B,30)
    b=a
    a=temp
  Next

  For t=60 To 79
    temp=sha1_rotateleft(A,5) + (B Xor C Xor D) + E + W[t] + K[3]
    e=d
    d=c
    c=sha1_rotateleft(B,30)
    b=a
    a=temp
  Next

  sha1_hash[0]=sha1_hash[0]+a
  sha1_hash[1]=sha1_hash[1]+b
  sha1_hash[2]=sha1_hash[2]+c
  sha1_hash[3]=sha1_hash[3]+d
  sha1_hash[4]=sha1_hash[4]+e

  sha1_index=0
End Function





;---------------------------------------------------------------------
Function sha1_reset()
  Local i

  sha1_low=0
  sha1_high=0
  sha1_index=0

  sha1_hash[0]=$67452301
  sha1_hash[1]=$EFCDAB89
  sha1_hash[2]=$98BADCFE
  sha1_hash[3]=$10325476
  sha1_hash[4]=$C3D2E1F0

  For i=0 To 63
    sha1_message[i]=0
  Next
End Function





;---------------------------------------------------------------------
;RETURN: sha1-fingerprint
;---------------------------------------------------------------------
Function sha1_result$()
  Local byte
  Local i
  Local txt$

  sha1_pad()

  For i=0 To 19
    byte=sha1_hash[i Shr 2] Shr 8 * (3-(i And 3))
    txt$=txt$+Chr$(byte)
  Next

  Return txt$
End Function





;---------------------------------------------------------------------
;value:  integer value
;shift:  bit shift
;RETURN: shifted value
;---------------------------------------------------------------------
Function sha1_rotateleft(value,shift)
  Return ((value Shl shift) Or (value Shr (32-shift)))
End Function