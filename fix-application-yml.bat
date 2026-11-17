@echo off
REM Fix application.yml files by removing duplicate jpa entries

setlocal enabledelayedexpansion

for /r \\D:\Github\Yatrik identity-service src\main\resources\*.yml (
    echo Processing: %~nxf

    (set /p content=<"%~f")

    REM Remove duplicate jpa entries and clean up
    set content=!content:  jpa:\n    hibernate:\n      ddl-auto: validate\n    database-platform: org.hibernate.dialect.H2Dialect\n    show-sql: false\n    properties:\n      hibernate:\n        jdbc:\n          time_zone: Asia/Kolkata\n        enable_lazy_load_no_trans: true\n        format_sql: true\n        use_sql_comments: true\n        # PostgreSQL compatibility settings\n        default_schema: public\n        implicit_naming_strategy: org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy\n        physical_naming_strategy: org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy\n        # Hibernate configuration to prevent enum casting issues\n        temp:\n          use_jdbc_metadata_for_column:true\n        # Additional JPA settings to prevent connection issues\n        jdbc:\n          time_zone: Asia/Kolkata! = ^
    set content=!content!

    REM Write back the fixed content
    (echo !content!) > "%~f"
)

echo Done fixing application.yml files.

REM Build the project
mkdir gateway\target\classes 2>nul
del identity-service\target\gateway.jar 2>nul
del fleet-service\target\gateway.jar 2>nul
del trip-service\target\gateway.jar 2>nul
del ledger-service\target\gateway.jar 2>nul
del reporting-service\target\gateway.jar 2>nul

echo Building project...
mvn clean compile -DskipTests -pl identity-service fleet-service trip-service ledger-service reporting-service