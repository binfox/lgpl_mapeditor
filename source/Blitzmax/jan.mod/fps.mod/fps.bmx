Strict
Import BRL.System

'Module Jan.FPS
'rem
ModuleInfo "Version: 1.00"
ModuleInfo "Author: Jan_(Jan Kuhnert)"
ModuleInfo "License: Public Domain"
ModuleInfo "Copyright: Keins, aber Credits währen nett"
'EndRem

Private

Global FPS_zahler%
Global FPS_Start_Time%
Global FPS_Time%
Global FPS#
Global FPS_Aktuell#

Public

Function update_FPS#(checktime%=250)
	
	Local Righttime%,Time_old%
	
	FPS_Zahler :+1 '= FPS_Zahler + 1
	
	If FPS_Time%=0 Then FPS_Time%=MilliSecs() 
	
	Time_old=FPS_Time%
		
	FPS_Time%=MilliSecs() 
	
	If FPS_Time%-time_old
		FPS_Aktuell# = 1000.0/Float(FPS_Time%-time_old)
	Else
		FPS_Aktuell# = FPS#
	EndIf
	'Print (FPS_Time%-time_old)
	
	If FPS_Time% > FPS_START_TIME + checktime%
	
		Righttime		= FPS_Time% - FPS_START_TIME%
		FPS_START_TIME	= FPS_Time%
		FPS#			= (Float(FPS_Zahler*1000.0)/Float(Righttime))
		FPS_Zahler		= 0
					
	EndIf
	
	Return FPS_Aktuell#

End Function

Function Get_User_Fps%()
	Return Int(FPS)
End Function

Function Get_Current_Fps#()
	Return FPS_Aktuell#
End Function

Rem
Repeat
	Delay Rand(50)
	Print String(Int(Update_FPS#())) + " x "+String(get_user_FPS())

Until KeyHit(Key_Escape)
EndRem
