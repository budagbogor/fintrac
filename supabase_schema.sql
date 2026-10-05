-- ============================================================
-- SUPABASE / POSTGRESQL DATABASE SCHEMA & RLS POLICIES
-- Project: Personal Finance & Smart Investment Advisor (FinAdvisor)
-- ============================================================

-- 1. EXTENSIONS & TYPES
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE income_type AS ENUM ('ROUTINE', 'NON_ROUTINE');
CREATE TYPE obligation_category AS ENUM ('RENT', 'MORTGAGE', 'LOAN_INSTALLMENT', 'UTILITIES', 'INSURANCE', 'SUBSCRIPTION', 'OTHER');
CREATE TYPE expense_category AS ENUM ('FOOD_BEVERAGE', 'TRANSPORTATION', 'ENTERTAINMENT', 'SHOPPING', 'HEALTHCARE', 'EDUCATION', 'OTHER');
CREATE TYPE goal_category AS ENUM ('EMERGENCY_FUND', 'HOUSE_DP', 'RETIREMENT', 'VEHICLE', 'EDUCATION', 'CUSTOM');
CREATE TYPE risk_profile_type AS ENUM ('CONSERVATIVE', 'MODERATE', 'AGGRESSIVE');
CREATE TYPE purchase_urgency AS ENUM ('WANT', 'NEED');
CREATE TYPE purchase_decision AS ENUM ('BUY_NOW', 'POSTPONE', 'AVOID');

-- 2. USER PROFILES TABLE
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT,
    risk_profile risk_profile_type DEFAULT 'MODERATE',
    monthly_target_savings_rate NUMERIC(5,2) DEFAULT 20.00, -- e.g. 20.00%
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. INCOMES TABLE
CREATE TABLE IF NOT EXISTS public.incomes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    amount NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    type income_type DEFAULT 'ROUTINE',
    source_category TEXT DEFAULT 'Gaji',
    income_date DATE DEFAULT CURRENT_DATE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. OBLIGATIONS (FIXED BILLS) TABLE
CREATE TABLE IF NOT EXISTS public.obligations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    amount NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    category obligation_category DEFAULT 'OTHER',
    due_day_of_month INT CHECK (due_day_of_month BETWEEN 1 AND 31),
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. EXPENSES (VARIABLE SPENDING) TABLE
CREATE TABLE IF NOT EXISTS public.expenses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    amount NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    category expense_category DEFAULT 'OTHER',
    expense_date DATE DEFAULT CURRENT_DATE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. FINANCIAL GOALS TABLE
CREATE TABLE IF NOT EXISTS public.financial_goals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    category goal_category DEFAULT 'CUSTOM',
    target_amount NUMERIC(15,2) NOT NULL CHECK (target_amount > 0),
    current_amount NUMERIC(15,2) DEFAULT 0 CHECK (current_amount >= 0),
    target_horizon_months INT NOT NULL CHECK (target_horizon_months > 0),
    risk_profile risk_profile_type DEFAULT 'MODERATE',
    expected_cagr_percentage NUMERIC(5,2) DEFAULT 8.00,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. SMART PURCHASE INQUIRIES TABLE
CREATE TABLE IF NOT EXISTS public.purchase_inquiries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    item_name TEXT NOT NULL,
    price NUMERIC(15,2) NOT NULL CHECK (price > 0),
    urgency purchase_urgency DEFAULT 'WANT',
    decision purchase_decision NOT NULL,
    postpone_months INT DEFAULT 0,
    opportunity_cost_3yr NUMERIC(15,2) DEFAULT 0,
    opportunity_cost_5yr NUMERIC(15,2) DEFAULT 0,
    ai_rationale TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 8. INDEXES FOR PERFORMANCE
CREATE INDEX IF NOT EXISTS idx_incomes_user_id ON public.incomes(user_id);
CREATE INDEX IF NOT EXISTS idx_expenses_user_id ON public.expenses(user_id);
CREATE INDEX IF NOT EXISTS idx_obligations_user_id ON public.obligations(user_id);
CREATE INDEX IF NOT EXISTS idx_goals_user_id ON public.financial_goals(user_id);
CREATE INDEX IF NOT EXISTS idx_purchases_user_id ON public.purchase_inquiries(user_id);

-- 9. ROW LEVEL SECURITY (RLS) POLICIES
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.incomes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.obligations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.expenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.financial_goals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.purchase_inquiries ENABLE ROW LEVEL SECURITY;

-- Profiles Policies
CREATE POLICY "Users can view own profile" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);

-- Incomes Policies
CREATE POLICY "Users can manage own incomes" ON public.incomes ALL USING (auth.uid() = user_id);

-- Obligations Policies
CREATE POLICY "Users can manage own obligations" ON public.obligations ALL USING (auth.uid() = user_id);

-- Expenses Policies
CREATE POLICY "Users can manage own expenses" ON public.expenses ALL USING (auth.uid() = user_id);

-- Financial Goals Policies
CREATE POLICY "Users can manage own goals" ON public.financial_goals ALL USING (auth.uid() = user_id);

-- Purchase Inquiries Policies
CREATE POLICY "Users can manage own purchase inquiries" ON public.purchase_inquiries ALL USING (auth.uid() = user_id);
