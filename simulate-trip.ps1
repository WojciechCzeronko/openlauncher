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
$updateIntervalMilliseconds = 1000

function Send-MockLocation {
    param(
        [double]$Latitude,
        [double]$Longitude,
        [double]$SpeedKmh,
        [double]$Bearing = 0
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

# Publish one mock location update every second.
# Position movement is calculated from the simulated speed and update interval.

$steps = @(
    @{ Speed = 0;  Bearing = 90 },
    @{ Speed = 20; Bearing = 90 },
    @{ Speed = 40; Bearing = 90 },
    @{ Speed = 50; Bearing = 90 },
    @{ Speed = 50; Bearing = 90 },

    # Right turn: east -> south
    @{ Speed = 50; Bearing = 105 },
    @{ Speed = 50; Bearing = 120 },
    @{ Speed = 50; Bearing = 135 },
    @{ Speed = 50; Bearing = 150 },
    @{ Speed = 50; Bearing = 165 },
    @{ Speed = 50; Bearing = 180 },

    @{ Speed = 50; Bearing = 180 },
    @{ Speed = 50; Bearing = 180 },
    @{ Speed = 50; Bearing = 180 },

    # Right turn: south -> west
    @{ Speed = 50; Bearing = 195 },
    @{ Speed = 50; Bearing = 210 },
    @{ Speed = 50; Bearing = 225 },
    @{ Speed = 50; Bearing = 240 },
    @{ Speed = 50; Bearing = 255 },
    @{ Speed = 50; Bearing = 270 },

    @{ Speed = 50; Bearing = 270 },
    @{ Speed = 30; Bearing = 270 },
    @{ Speed = 10; Bearing = 270 },
    @{ Speed = 0;  Bearing = 270 }
)

foreach ($step in $steps) {

    $speed = $step.Speed
    $bearing = $step.Bearing

    if ($speed -gt 0) {
        $distanceMeters =
            ($speed / 3.6) * ($updateIntervalMilliseconds/1000)

        $bearingRadians =
            $bearing * [Math]::PI / 180.0

        $metersPerDegreeLatitude = 111320.0

        $metersPerDegreeLongitude =
            111320.0 * [Math]::Cos(
                $lat * [Math]::PI / 180.0
            )

        $latDelta =
            ($distanceMeters * [Math]::Cos($bearingRadians)) /
            $metersPerDegreeLatitude

        $lonDelta =
            ($distanceMeters * [Math]::Sin($bearingRadians)) /
            $metersPerDegreeLongitude

        $lat += $latDelta
        $lon += $lonDelta
    }

    Send-MockLocation `
        -Latitude $lat `
        -Longitude $lon `
        -SpeedKmh $speed `
        -Bearing $bearing

    Start-Sleep -Milliseconds $updateIntervalMilliseconds
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