# simulate-trip.ps1

$adb = "F:\android-sdk\platform-tools\adb.exe"

$package = "io.appium.settings"
$service = "io.appium.settings/.LocationService"

# starting point
$lat = 53.1381
$lon = 18.0220
$alt = 80
$bearing = 90
$accuracy = 5

function Send-MockLocation {
    param(
        [double]$Latitude,
        [double]$Longitude,
        [double]$SpeedKmh,
        [double]$Bearing = 90
    )

    $speedMps = $SpeedKmh / 3.6

    Write-Host (
        "LAT={0:F6} LON={1:F6} SPEED={2:F0} km/h" -f `
        $Latitude, $Longitude, $SpeedKmh
    )

    & $adb shell am start-foreground-service `
        --user 0 `
        -n $service `
        --es longitude "$Longitude" `
        --es latitude "$Latitude" `
        --es altitude "$alt" `
        --es speed "$speedMps" `
        --es bearing "$Bearing" `
        --es accuracy "$accuracy" | Out-Null
}

Write-Host "Enabling mock location..."
& $adb shell appops set $package android:mock_location allow

Write-Host "Starting simulated trip..."

# every step last 3 seconds.
# after each step we move longitude a bit to the east.

$steps = @(
    0, 0,
    10, 20, 30, 40, 50,
    50, 50, 50, 50, 50,
    60, 70, 80, 90,
    90, 90, 90, 90, 90,
    80, 70, 60, 50,
    40, 30, 20, 10,
    0, 0
)

foreach ($speed in $steps) {

    # approximate GPS movement
    # higher speed = bigger step
    if ($speed -gt 0) {
        $distanceMeters = ($speed / 3.6) * 3.0

        # around 1 degree longitude at this coordinates
        # ~67 km
        $lonDelta = $distanceMeters / 67000.0

        $lon += $lonDelta
    }

    Send-MockLocation `
        -Latitude $lat `
        -Longitude $lon `
        -SpeedKmh $speed `
        -Bearing $bearing

    Start-Sleep -Seconds 1
}

Write-Host "Stopping..."

Send-MockLocation `
    -Latitude $lat `
    -Longitude $lon `
    -SpeedKmh 0 `
    -Bearing $bearing

Start-Sleep -Seconds 2

Write-Host ""
Write-Host "Trip finished."
Write-Host "Final position:"
Write-Host ("LAT={0:F6} LON={1:F6}" -f $lat, $lon)