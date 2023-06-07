
for /F %%I in ('dir /b /o:-d target\*.jar') do ( set file=%%I & goto :end )

:end

java -jar target\%file%
