-- Update Service Descriptions to English
-- This script updates Malay service descriptions to English

USE MizrahBeauty;
GO

-- Update Body Scrub + Massage description
UPDATE services 
SET details = 'Combined treatment that combines the exfoliation process with relaxing massage therapy for complete body wellness.'
WHERE service_name = 'Body Scrub + Massage';

-- Update Foot Massage description
UPDATE services 
SET details = 'Massage treatment that focuses specifically on the feet and soles to relieve tension and improve circulation.'
WHERE service_name = 'Foot Massage';

-- Update Full Body Massage description
UPDATE services 
SET details = 'Comprehensive massage technique that involves the entire body to promote relaxation and relieve muscle tension.'
WHERE service_name = 'Full Body Massage';

-- Update Herbal Sauna description
UPDATE services 
SET details = 'Traditional treatment that uses mixed hot steam with herbal ingredients to detoxify and rejuvenate the body.'
WHERE service_name = 'Herbal Sauna';

-- Update any other services that might have Malay descriptions
UPDATE services 
SET details = CASE 
    WHEN details LIKE '%Rawatan%' THEN REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
        details,
        'Rawatan', 'Treatment'),
        'gabungan', 'combined'),
        'menggabungkan', 'combining'),
        'pengelupas', 'exfoliation'),
        'urutan', 'massage'),
        'memberi fokus', 'focusing'),
        'khusus', 'specifically'),
        'bahagian kaki', 'feet area'),
        'tapak', 'soles'),
        'menyeluruh', 'comprehensive'),
        'melibatkan', 'involving'),
        'seluruh bahagian', 'entire body'),
        'tradisional', 'traditional'),
        'menggunakan wap', 'using steam'),
        'panas bercampur', 'mixed hot'),
        'detoksifikasi', 'detoxification'),
        'menyegarkan', 'rejuvenating')
    ELSE details
END
WHERE details LIKE '%Rawatan%' OR details LIKE '%urutan%' OR details LIKE '%tradisional%';

PRINT 'Service descriptions updated to English successfully!';
GO
