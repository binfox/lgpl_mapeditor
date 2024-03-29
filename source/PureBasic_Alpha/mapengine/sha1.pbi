Global sha1_low.l, sha1_high.l, sha1_index.l
Dim sha1_hash.l(4)
Dim sha1_message.l(63)





; ---------------------------------------------------------------------
;value:  integer value
;shift:  bit shift
;RETURN: shifted value
; ---------------------------------------------------------------------
Procedure sha1_rotateleft(valu.l, shift.l)
  ProcedureReturn ((value << shift) | (value >> (32 - shift)))
EndProcedure






; ---------------------------------------------------------------------
Procedure sha1_process()
  Protected a.l, b.l, c.l, d.l, e.l, t.l, temp.l
  Dim k.l(3)
  Dim w.l(79)
  
  k(0) = $5A827999
  k(1) = $6ED9EBA1
  k(2) = $8F1BBCDC
  k(3) = $CA62C1D6
  
  For t = 0 To 15
    w(t) = sha1_message(t * 4) << 24
    w(t) | (sha1_message(t * 4 + 1) << 16)
    w(t) | (sha1_message(t * 4 + 2) << 8)
    w(t) | (sha1_message(t * 4 + 3)
  Next
  
  For t = 16 To 79
    w(t) = sha1_rotateleft(w(t - 3) ! w(t - 8) ! w(t - 14) ! w(t - 16), 1)
  Next
  
  a = sha1_hash(0)
  b = sha1_hash(1)
  c = sha1_hash(2)
  d = sha1_hash(3)
  e = sha1_hash(4)
  
  For t = 0 To 19
    temp = sha1_rotateleft(a, 5) + ((b & c) | (((~b) & d) + e + w(t) + k(0)))
    e = d
    d = c
    c = sha1_rotateleft(b, 30)
    b = a
    a = temp
  Next
  
  For t = 20 To 39
    temp = sha1_rotateleft(a, 5) + (b ! c ! d) + e + w(t) + k(1)
    e = d
    d = c
    c = sha1_rotateleft(b, 30)
    b = a
    a = temp
  Next
  
  For t = 40 To 59
    temp = sha1_rotateleft(a, 5) + ((b & c) | (b & d) | (c & d)) + e + w(t) + k(2)
    e = d
    d = c
    c = sha1_rotateleft(b, 30)
    b = a
    a = temp
  Next
  
  For t = 60 To 79
    temp = sha1_rotateleft(a, 5) + (b ! c ! d) + e + w(t) + k(3)
    e = d
    d = c
    c = sha1_rotateleft(b, 30)
    b = a
    a = temp
  Next
  
  sha1_hash(0) + a
  sha1_hash(1) + b
  sha1_hash(2) + c
  sha1_hash(3) + d
  sha1_hash(4) + e
  
  sha1_index = 0
EndProcedure






; ---------------------------------------------------------------------
Procedure sha1_pad()
  If sha1_index > 55
    
    sha1_message(sha1_index) = $80
    sha1_index + 1
    
    While sha1_index < 64
      sha1_message(sha1_index) = 0
      sha1_index + 1
    Wend
    
  Else
    
    sha1_message(sha1_index) = $80
    sha1_index + 1
    
    While sha1_index < 56
      sha1_message(sha1_index) = 0
      sha1_index + 1
    Wend
  
  EndIf
  
  sha1_message(56) = sha1_high >> 24
  sha1_message(57) = sha1_high >> 16
  sha1_message(58) = sha1_high >> 8
  sha1_message(59) = sha1_high
  sha1_message(60) = sha1_low >> 24
  sha1_message(61) = sha1_low >> 16
  sha1_message(62) = sha1_low >> 8
  sha1_message(63) = sha1_low
  
  sha1_process()
EndProcedure






; ---------------------------------------------------------------------
;RETURN: sha1-fingerprint
; ---------------------------------------------------------------------
Procedure.s sha1_result()
  Protected byte.l, i.l, txt.s
  
  sha1_pad()
  
  For i = 0 To 19
    byte = sha1_hash(i >> 2) Shr (8 * (3 - (i & 3)))
    txt = txt + Chr(byte)
  Next
  
  ProcedureReturn txt
EndProcedure






; ---------------------------------------------------------------------
Procedure sha1_reset()
  Protected i.l
  
  sha1_low = 0
  sha1_high = 0
  sha1_index = 0
  
  sha1_hash(0) = $67452301
  sha1_hash(1) = $EFCDAB89
  sha1_hash(2) = $98BADCFE
  sha1_hash(3) = $10325476
  sha1_hash(4) = $C3D2E1F0
  
  For i = 0 To 63
    sha1_message(i) = 0
  Next
EndProcedure






; ---------------------------------------------------------------------
;txt:    text string
;RETURN: sha1-fingerprint (20 byte)
; ---------------------------------------------------------------------
Procedure.s sha1(txt.s)
  Protected ascii.l, i.l, Length.l
  
  sha1_reset()
  Length = Len(txt)
  
  For i = 1 To Length
    ascii = Asc(Mid(txt, i, 1))
    sha1_message(sha1_index) = ascii
    sha1_index = sha1_index + 1
    
    sha1_low + 8
    If sha1_low = 0
      sha1_high + 1
      If sha1_high = 0 : ProcedureReturn #False : EndIf
    EndIf
    
    If sha1_index = 64 : sha1_process()
  Next
  
  ProcedureReturn sha1_result()
EndProcedure 
; jaPBe Version=1.4.4.25
; FoldLines=000D000F0017005D006500860090009B00A300B300BE00D3
; Build=0
; CompileThis=..\preview_bbplus.pb
; FirstLine=0
; CursorPosition=190
; ExecutableFormat=Windows
; DontSaveDeclare
; EOF