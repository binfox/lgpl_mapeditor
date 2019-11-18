Dim crc32_table(255)





;---------------------------------------------------------------------
;name:    file name
;RETURN:  crc32 value
;---------------------------------------------------------------------
Function crc32_file(name$)
  Local bank
  Local byte
  Local bytes
  Local crc32
  Local file
  Local fsize
  Local i
  Local j

  crc32_init()
  crc32=$FFFFFFFF

  fsize=FileSize(name$)
  If fsize<8 Then Return

  file=ReadFile(name$)
  If file=0 Then Return
  SeekFile file,8
  bank=CreateBank($4000)

  For j=1 To (fsize-8)/$4000
    ReadBytes bank,file,0,$4000
    For i=0 To $3FFF
      byte=PeekByte(bank,i)
      crc32=(crc32 Shr 8) Xor crc32_table(byte Xor (crc32 And $FF))
    Next
  Next

  bytes=(fsize-8) Mod $4000
  If bytes>0 Then
    ReadBytes bank,file,0,bytes
    For i=0 To bytes-1
      byte=PeekByte(bank,i)
      crc32=(crc32 Shr 8) Xor crc32_table(byte Xor (crc32 And $FF))
    Next
  EndIf

  CloseFile file
  FreeBank bank
  Return ~crc32
End Function





;---------------------------------------------------------------------
Function crc32_init()
  Local i
  Local j
  Local value

  For i=0 To 255
    value=i
    For j=0 To 7
      If (value And 1) Then 
        value=(value Shr 1) Xor $EDB88320
      Else
        value=(value Shr 1)
      EndIf
    Next
    crc32_table(i)=value
  Next
End Function