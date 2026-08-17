import React from 'react';

export const TrustGauge: React.FC<{
  score: number;
  size?: 'sm' | 'md' | 'lg';
  showLabel?: boolean;
}> = ({ score, size = 'md', showLabel = true }) => {
  const clampedScore = Math.max(0, Math.min(100, score));

  // Determine color theme based on Zero Trust score threshold
  let strokeColor = '#10B981'; // green (>= 80)
  let textColor = 'text-emerald-400';
  let label = 'Nominal';

  if (clampedScore < 35) {
    strokeColor = '#F43F5E'; // rose/red (< 35)
    textColor = 'text-rose-400';
    label = 'Critical';
  } else if (clampedScore < 60) {
    strokeColor = '#F97316'; // orange (< 60)
    textColor = 'text-orange-400';
    label = 'Suspicious';
  } else if (clampedScore < 80) {
    strokeColor = '#FBBF24'; // amber (< 80)
    textColor = 'text-amber-400';
    label = 'Elevated';
  }

  const dimensions = {
    sm: { radius: 24, stroke: 4, text: 'text-sm font-bold', sub: 'text-[9px]' },
    md: { radius: 40, stroke: 6, text: 'text-xl font-bold', sub: 'text-xs' },
    lg: { radius: 64, stroke: 8, text: 'text-3xl font-extrabold', sub: 'text-sm' },
  }[size];

  const circumference = 2 * Math.PI * dimensions.radius;
  const strokeDashoffset = circumference - (clampedScore / 100) * circumference;

  return (
    <div className="flex flex-col items-center justify-center">
      <div className="relative inline-flex items-center justify-center">
        <svg
          className="transform -rotate-90"
          width={(dimensions.radius + dimensions.stroke) * 2}
          height={(dimensions.radius + dimensions.stroke) * 2}
        >
          {/* Background track */}
          <circle
            cx={dimensions.radius + dimensions.stroke}
            cy={dimensions.radius + dimensions.stroke}
            r={dimensions.radius}
            stroke="#1F2937"
            strokeWidth={dimensions.stroke}
            fill="transparent"
          />
          {/* Active progress bar */}
          <circle
            cx={dimensions.radius + dimensions.stroke}
            cy={dimensions.radius + dimensions.stroke}
            r={dimensions.radius}
            stroke={strokeColor}
            strokeWidth={dimensions.stroke}
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            strokeLinecap="round"
            fill="transparent"
            className="transition-all duration-700 ease-out"
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className={`${dimensions.text} ${textColor} font-mono tracking-tight`}>
            {clampedScore}
          </span>
          {showLabel && (
            <span className={`${dimensions.sub} text-gray-400 font-medium -mt-1`}>
              /100
            </span>
          )}
        </div>
      </div>
      {showLabel && size !== 'sm' && (
        <span className={`mt-1 text-xs font-semibold ${textColor}`}>
          {label}
        </span>
      )}
    </div>
  );
};
