Dim md5_table(0)





;---------------------------------------------------------------------
;message: message string
;RETURN:  md5-fingerprint (16 byte)
;---------------------------------------------------------------------
Function md5$(message$)
  Local ascii
  Local blocks
  Local i
  Local length
  Local md5_a
  Local md5_b
  Local md5_c
  Local md5_d

  length=Len(message$)
  blocks=((length+8) Shr 6)+1

  Dim md5_table(blocks*16-1)

  For i=0 To length-1
    ascii=Asc(Mid(message$,i+1,1))
    md5_table((i Shr 2))=md5_table((i Shr 2)) Or (ascii Shl ((i Mod 4)*8))
  Next 

  md5_table((i Shr 2))=md5_table((i Shr 2)) Or (128 Shl ((i Mod 4)*8))
  md5_table(blocks*16-2)=length*8
    
  md5_a = $67452301
  md5_b = $EFCDAB89
  md5_c = $98BADCFE
  md5_d = $10325476

  For i=0 To blocks*16-1 Step 16
    md5_aa = md5_a
    md5_bb = md5_b
    md5_cc = md5_c
    md5_dd = md5_d

    md5_a = md5_ff(md5_a, md5_b, md5_c, md5_d, md5_table(i+00), 07, $D76AA478)
    md5_d = md5_ff(md5_d, md5_a, md5_b, md5_c, md5_table(i+01), 12, $E8C7B756)
    md5_c = md5_ff(md5_c, md5_d, md5_a, md5_b, md5_table(i+02), 17, $242070DB)
    md5_b = md5_ff(md5_b, md5_c, md5_d, md5_a, md5_table(i+03), 22, $C1BDCEEE)
    md5_a = md5_ff(md5_a, md5_b, md5_c, md5_d, md5_table(i+04), 07, $F57C0FAF)
    md5_d = md5_ff(md5_d, md5_a, md5_b, md5_c, md5_table(i+05), 12, $4787C62A)
    md5_c = md5_ff(md5_c, md5_d, md5_a, md5_b, md5_table(i+06), 17, $A8304613)
    md5_b = md5_ff(md5_b, md5_c, md5_d, md5_a, md5_table(i+07), 22, $FD469501)
    md5_a = md5_ff(md5_a, md5_b, md5_c, md5_d, md5_table(i+08), 07, $698098D8)
    md5_d = md5_ff(md5_d, md5_a, md5_b, md5_c, md5_table(i+09), 12, $8B44F7AF)
    md5_c = md5_ff(md5_c, md5_d, md5_a, md5_b, md5_table(i+10), 17, $FFFF5BB1)
    md5_b = md5_ff(md5_b, md5_c, md5_d, md5_a, md5_table(i+11), 22, $895CD7BE)
    md5_a = md5_ff(md5_a, md5_b, md5_c, md5_d, md5_table(i+12), 07, $6B901122)
    md5_d = md5_ff(md5_d, md5_a, md5_b, md5_c, md5_table(i+13), 12, $FD987193)
    md5_c = md5_ff(md5_c, md5_d, md5_a, md5_b, md5_table(i+14), 17, $A679438E)
    md5_b = md5_ff(md5_b, md5_c, md5_d, md5_a, md5_table(i+15), 22, $49B40821)

    md5_a = md5_gg(md5_a, md5_b, md5_c, md5_d, md5_table(i+01), 05, $F61E2562)
    md5_d = md5_gg(md5_d, md5_a, md5_b, md5_c, md5_table(i+06), 09, $C040B340)
    md5_c = md5_gg(md5_c, md5_d, md5_a, md5_b, md5_table(i+11), 14, $265E5A51)
    md5_b = md5_gg(md5_b, md5_c, md5_d, md5_a, md5_table(i+00), 20, $E9B6C7AA)
    md5_a = md5_gg(md5_a, md5_b, md5_c, md5_d, md5_table(i+05), 05, $D62F105D)
    md5_d = md5_gg(md5_d, md5_a, md5_b, md5_c, md5_table(i+10), 09, $02441453)
    md5_c = md5_gg(md5_c, md5_d, md5_a, md5_b, md5_table(i+15), 14, $D8A1E681)
    md5_b = md5_gg(md5_b, md5_c, md5_d, md5_a, md5_table(i+04), 20, $E7D3FBC8)
    md5_a = md5_gg(md5_a, md5_b, md5_c, md5_d, md5_table(i+09), 05, $21E1CDE6)
    md5_d = md5_gg(md5_d, md5_a, md5_b, md5_c, md5_table(i+14), 09, $C33707D6)
    md5_c = md5_gg(md5_c, md5_d, md5_a, md5_b, md5_table(i+03), 14, $F4D50D87)
    md5_b = md5_gg(md5_b, md5_c, md5_d, md5_a, md5_table(i+08), 20, $455A14ED)
    md5_a = md5_gg(md5_a, md5_b, md5_c, md5_d, md5_table(i+13), 05, $A9E3E905)
    md5_d = md5_gg(md5_d, md5_a, md5_b, md5_c, md5_table(i+02), 09, $FCEFA3F8)
    md5_c = md5_gg(md5_c, md5_d, md5_a, md5_b, md5_table(i+07), 14, $676F02D9)
    md5_b = md5_gg(md5_b, md5_c, md5_d, md5_a, md5_table(i+12), 20, $8D2A4C8A)

    md5_a = md5_hh(md5_a, md5_b, md5_c, md5_d, md5_table(i+05), 04, $FFFA3942)
    md5_d = md5_hh(md5_d, md5_a, md5_b, md5_c, md5_table(i+08), 11, $8771F681)
    md5_c = md5_hh(md5_c, md5_d, md5_a, md5_b, md5_table(i+11), 16, $6D9D6122)
    md5_b = md5_hh(md5_b, md5_c, md5_d, md5_a, md5_table(i+14), 23, $FDE5380C)
    md5_a = md5_hh(md5_a, md5_b, md5_c, md5_d, md5_table(i+01), 04, $A4BEEA44)
    md5_d = md5_hh(md5_d, md5_a, md5_b, md5_c, md5_table(i+04), 11, $4BDECFA9)
    md5_c = md5_hh(md5_c, md5_d, md5_a, md5_b, md5_table(i+07), 16, $F6BB4B60)
    md5_b = md5_hh(md5_b, md5_c, md5_d, md5_a, md5_table(i+10), 23, $BEBFBC70)
    md5_a = md5_hh(md5_a, md5_b, md5_c, md5_d, md5_table(i+13), 04, $289B7EC6)
    md5_d = md5_hh(md5_d, md5_a, md5_b, md5_c, md5_table(i+00), 11, $EAA127FA)
    md5_c = md5_hh(md5_c, md5_d, md5_a, md5_b, md5_table(i+03), 16, $D4EF3085)
    md5_b = md5_hh(md5_b, md5_c, md5_d, md5_a, md5_table(i+06), 23, $04881D05)
    md5_a = md5_hh(md5_a, md5_b, md5_c, md5_d, md5_table(i+09), 04, $D9D4D039)
    md5_d = md5_hh(md5_d, md5_a, md5_b, md5_c, md5_table(i+12), 11, $E6DB99E5)
    md5_c = md5_hh(md5_c, md5_d, md5_a, md5_b, md5_table(i+15), 16, $1FA27CF8)
    md5_b = md5_hh(md5_b, md5_c, md5_d, md5_a, md5_table(i+02), 23, $C4AC5665)

    md5_a = md5_ii(md5_a, md5_b, md5_c, md5_d, md5_table(i+00), 06, $F4292244)
    md5_d = md5_ii(md5_d, md5_a, md5_b, md5_c, md5_table(i+07), 10, $432AFF97)
    md5_c = md5_ii(md5_c, md5_d, md5_a, md5_b, md5_table(i+14), 15, $AB9423A7)
    md5_b = md5_ii(md5_b, md5_c, md5_d, md5_a, md5_table(i+05), 21, $FC93A039)
    md5_a = md5_ii(md5_a, md5_b, md5_c, md5_d, md5_table(i+12), 06, $655B59C3)
    md5_d = md5_ii(md5_d, md5_a, md5_b, md5_c, md5_table(i+03), 10, $8F0CCC92)
    md5_c = md5_ii(md5_c, md5_d, md5_a, md5_b, md5_table(i+10), 15, $FFEFF47D)
    md5_b = md5_ii(md5_b, md5_c, md5_d, md5_a, md5_table(i+01), 21, $85845DD1)
    md5_a = md5_ii(md5_a, md5_b, md5_c, md5_d, md5_table(i+08), 06, $6FA87E4F)
    md5_d = md5_ii(md5_d, md5_a, md5_b, md5_c, md5_table(i+15), 10, $FE2CE6E0)
    md5_c = md5_ii(md5_c, md5_d, md5_a, md5_b, md5_table(i+06), 15, $A3014314)
    md5_b = md5_ii(md5_b, md5_c, md5_d, md5_a, md5_table(i+13), 21, $4E0811A1)
    md5_a = md5_ii(md5_a, md5_b, md5_c, md5_d, md5_table(i+04), 06, $F7537E82)
    md5_d = md5_ii(md5_d, md5_a, md5_b, md5_c, md5_table(i+11), 10, $BD3AF235)
    md5_c = md5_ii(md5_c, md5_d, md5_a, md5_b, md5_table(i+02), 15, $2AD7D2BB)
    md5_b = md5_ii(md5_b, md5_c, md5_d, md5_a, md5_table(i+09), 21, $EB86D391)

    md5_a = md5_a + md5_aa
    md5_b = md5_b + md5_bb
    md5_c = md5_c + md5_cc
    md5_d = md5_d + md5_dd
  Next
    
  Return Lower$(md5_int2chr$(md5_a) + md5_int2chr$(md5_b) + md5_int2chr$(md5_c) + md5_int2chr$(md5_d))
End Function





;---------------------------------------------------------------------
;x,y,z:  old values
;RETURN: new value
;---------------------------------------------------------------------
Function md5_f(x, y, z)
  Return (x And y) Or ((~x) And z)
End Function





;---------------------------------------------------------------------
;x,y,z:  old values
;RETURN: new value
;---------------------------------------------------------------------
Function md5_g(x, y, z)
  Return (x And z) Or (y And (~z))
End Function





;---------------------------------------------------------------------
;x,y,z:  old values
;RETURN: new value
;---------------------------------------------------------------------
Function md5_h(x, y, z)
  Return (x Xor y Xor z)
End Function





;---------------------------------------------------------------------
;x,y,z:  old values
;RETURN: new value
;---------------------------------------------------------------------
Function md5_i(x, y, z)
  Return (y Xor (x Or (~z)))
End Function





;---------------------------------------------------------------------
;a,b,c,d,x: old values
;s:         bit shift
;ac:        accumulator
;RETURN:    new value
;---------------------------------------------------------------------
Function md5_ff(a, b, c, d, x, s, ac)
  a = (a + ((md5_f(b, c, d) + x) + ac))
  a = md5_rotateleft(a, s)
  Return a + b
End Function





;---------------------------------------------------------------------
;a,b,c,d,x: old values
;s:         bit shift
;ac:        accumulator
;RETURN:    new value
;---------------------------------------------------------------------
Function md5_gg(a, b, c, d, x, s, ac)
  a = (a + ((md5_g(b, c, d) + x) + ac))
  a = md5_rotateleft(a, s)
  Return a + b
End Function





;---------------------------------------------------------------------
;a,b,c,d,x: old values
;s:         bit shift
;ac:        accumulator
;RETURN:    new value
;---------------------------------------------------------------------
Function md5_hh(a, b, c, d, x, s, ac)
  a = (a + ((md5_h(b, c, d) + x) + ac))
  a = md5_rotateleft(a, s)
  Return a + b
End Function





;---------------------------------------------------------------------
;a,b,c,d,x: old values
;s:         bit shift
;ac:        accumulator
;RETURN:    new value
;---------------------------------------------------------------------
Function md5_ii(a, b, c, d, x, s, ac)
  a = (a + ((md5_i(b, c, d) + x) + ac))
  a = md5_rotateleft(a, s)
  Return a + b
End Function





;---------------------------------------------------------------------
;value:  integer value
;shift:  bit shift
;RETURN: shifted value
;---------------------------------------------------------------------
Function md5_rotateleft(value,shift)
  Return (value Shl shift) Or (value Shr (32-shift))
End Function





;---------------------------------------------------------------------
;value:  integer value
;RETURN: 4 byte string
;---------------------------------------------------------------------
Function md5_int2chr$(value)
  Local byte
  Local i
  Local txt$

  For i=0 To 3
    byte=(value Shr i*8) And 255
    txt$=txt$+Chr$(byte)
  Next
  Return txt$
End Function