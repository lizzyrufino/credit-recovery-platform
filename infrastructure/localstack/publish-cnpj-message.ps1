param(
    [Parameter(Mandatory = $true)]
    [string] $Cnpj,

    [string] $QueueName = "credit-profile-received",
    [string] $Region = "us-east-1",
    [string] $ContainerName = "credit-recovery-localstack",
    [string] $EventId = "evt-credit-profile-$([guid]::NewGuid().ToString())",
    [string] $CorrelationId = "corr-manual-$([guid]::NewGuid().ToString())",
    [int] $DaysOverdue = 87,
    [decimal] $DebtAmount = 125000.50,
    [int] $InternalScore = 812,
    [string] $RiskLevel = "HIGH",
    [string] $PreferredChannel = "WHATSAPP",
    [bool] $WhatsappConsent = $true,
    [bool] $ActivePjCard = $true
)

$ErrorActionPreference = "Stop"

$normalizedCnpj = $Cnpj -replace "\D", ""
if ($normalizedCnpj.Length -ne 14) {
    throw "CNPJ invalido. Informe 14 digitos, com ou sem mascara."
}

$queueUrl = docker exec $ContainerName awslocal sqs get-queue-url `
    --queue-name $QueueName `
    --region $Region `
    --query QueueUrl `
    --output text

$message = [ordered]@{
    eventId       = $EventId
    correlationId = $CorrelationId
    occurredAt    = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    profile       = [ordered]@{
        document         = [ordered]@{
            value = $normalizedCnpj
        }
        daysOverdue      = $DaysOverdue
        debtAmount       = $DebtAmount
        products         = @(
            [ordered]@{
                type              = "CREDIT_CARD_PJ"
                active            = $true
                outstandingAmount = [decimal]85000.00
            },
            [ordered]@{
                type              = "WORKING_CAPITAL"
                active            = $true
                outstandingAmount = [decimal]40500.50
            }
        )
        internalScore    = $InternalScore
        paymentHistory   = [ordered]@{
            paidInstallments    = 12
            delayedInstallments = 3
            debtRegularized     = $false
        }
        preferredChannel = $PreferredChannel
        whatsappConsent  = $WhatsappConsent
        riskLevel        = $RiskLevel
        activePjCard     = $ActivePjCard
    }
}

$messageBody = $message | ConvertTo-Json -Depth 10 -Compress
$messageFile = "/tmp/sqs-message-$($EventId -replace '[^A-Za-z0-9_.-]', '_').json"

$messageBody | docker exec -i $ContainerName sh -c "cat > '$messageFile'"

docker exec $ContainerName awslocal sqs send-message `
    --queue-url $queueUrl `
    --message-body "file://$messageFile" `
    --region $Region

docker exec $ContainerName rm -f $messageFile

Write-Host "Mensagem publicada no SQS."
Write-Host "QueueUrl: $queueUrl"
Write-Host "CNPJ: $normalizedCnpj"
Write-Host "EventId: $EventId"
Write-Host "CorrelationId: $CorrelationId"
