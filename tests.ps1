# Output log file
$log = "cli_test_output.txt"
Remove-Item $log -ErrorAction Ignore

# Define commands
$commands = @(
    'auth signup -u alice -p pass123 --confirm-password pass123 --role Reader',
    'auth signin  -u alice -p pass123',
    'auth signout',
    
    'user list',
    'user delete -u alice',
    'user promote -u bob --to Librarian',
    'user demote  -u bob --to Reader',
    'user suspend -u charlie',
    'user activate -u charlie',
    
    'book add    --title=1984 --author=Orwell --copies=5',
    'book delete --id=42',
    'book update --id=42 --title=Brave New World',
    'book list',
    'book search --title=Potter',
    'book stats  --id=42',
    
    'borrow request --book-id=42',
    'borrow approve --user=alice --book-id=42',
    'borrow reject  --user=alice --book-id=42',
    'borrow return  --book-id=42',
    'borrow list',
    'borrow history',
    
    
    'profile view',
    'profile update --name=\"Alice A.\"',
    'profile password --old=pass123 --new=newpass --confirm=newpass',
    
    'report top-borrowed',
    'report top-fines',
    'report category-distribution',
    
    'system init --confirm=yes',
    'system stats',
    'system overdue',
    'system report'
)

# Run each command and log output
foreach ($cmd in $commands) {
    $full = ".\gradlew.bat run --quiet --args=`"$cmd`""
    "`n`n> $full`n" | Out-File -FilePath $log -Append -Encoding utf8
    Invoke-Expression $full | Out-File -FilePath $log -Append -Encoding utf8
}

