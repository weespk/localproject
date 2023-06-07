
for /F %%I in ('dir /b /o:-d target\*.war') do ( set file=%%I & goto :end )

:end
start /b javaw -jar target\%file%
pause